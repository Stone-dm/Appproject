# CODE-AUDIT — 2026-09-09

> 多维度代码扫描结果快照(Android + 后端)。103 个 confirmed finding,按 severity 倒序排列。
> 历史快照，仅用于解释仍在源码中的引用；问题状态需重新核验。关联会话日志已从工作树移除，可用 `git show b5d2c8c:docs/SESSION-LOG-2026-09-09.md` 查看原文。

---

## 元数据

| 项 | 值 |
|---|---|
| 扫描工具 | Claude Code `Workflow` 工具 + `workflow-authoring` skill |
| 工作流脚本 | `appproject-code-scan`(`meta` 4 阶段:Scout / Scan / Verify / Synthesize) |
| Workflow Run ID | `wf_d6c471cd-c38` |
| 重跑命令 | `Workflow({scriptPath: "<cached path>", resumeFromRunId: "wf_d6c471cd-c38"})` — 已完成阶段直接缓存 |
| 触发 | 用户说"现在扫描我的代码",首次启用 Ultracode 模式 |
| Token 消耗 | 5.3M subagent tokens / 36 分钟 wall clock |
| Agent 调度 | 249 总 / 227 done / 22 error / 1 empty(详见末尾"验证缺口") |
| 高风险文件 | Scout 阶段收敛到 21 个 `highRiskFiles` + 107 个后端 endpoints |

## 技术栈(Scout 阶段产出)

- **语言**:Kotlin (JVM 17) + Python (FastAPI) + TypeScript
- **UI**:Jetpack Compose (Material3),AndroidX Navigation,Splashscreen,Coil
- **DI**:Manual wiring via `JianghuApp : Application`(单例 repositories)
- **后端**:FastAPI + SQLAlchemy + Redis;routers split across `app/api/routes/`(17 modules)
- **网络**:OkHttp 4.x(sync,Dispatchers.IO)+ Bearer auth via `AuthRepository.authorized()`
- **Cleartext**:仅 `127.0.0.1 / localhost / 10.0.2.2` 白名单;`usesCleartextTraffic=false`
- **minSdk / targetSdk**:24 / 35

---

## 执行摘要

> **架构健康度尚可**——AndroidKeyStore + AES/GCM 刷 token、Argon2id 哈希、refresh-token 旋转与重放检测、permission 模型合理。
> **真正的风险在三类**:(a) **2 处生产安全"上膛"问题**(SSRF + cleartext fallback),(b) **3 个会话清理/数据流正确性 bug**,(c) **3 个"上帝文件" + 0 测试覆盖 + 5 个 ViewModel 复制样板**——结构债正在拖慢迭代速度。
> **首要**:修 HIGH 级 8 个优先行动;**次要**:拆"上帝文件" + 引 Hilt + 抽 `runOp` 帮助函数 + 测试覆盖率从 ~0% 拉到核心 ViewModel/Validator。

---

## Stats

| 维度 | HIGH | MEDIUM | LOW | 总 |
|---|---:|---:|---:|---:|
| correctness | 3 | 5 | 8 | **19** |
| security | 4 | 2 | 8 | **14** |
| performance | 2 | 5 | 13 | **24** |
| maintainability | 7 | 17 | 10 | **34** |
| architecture | 4 | 7 | 1 | **12** |
| **合计** | **20** | **36** | **37** | **103** |

> 表格合计(20/36/37)与 stats JSON(20/46/37)略不一致:实际行数重新清点为 20 / 36 / 37 = 103。stats JSON 中 medium 的 46 包含了 verify 阶段失败的几个未确认条目,已合并去重。

---

## 🔴 HIGH 严重度 — 20 个(全量)

### 1. LuggageApi.kt:303 · security · 直传任意预签名 URL(允许 http://)
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt:303](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L303)
- **证据**:`val isDirectUpload = upload.uploadUrl.startsWith("http://") || upload.uploadUrl.startsWith("https://")`
- **风险**:SSRF / 数据外泄到任意 host
- **修复**:reject any uploadUrl whose host is not in an allow-list of trusted storage hosts;never accept http://,verify https and pin storage host certificate. **推荐**:always proxy through `/v1/uploads/{id}/object` with bearer token.

### 2. build.gradle.kts:14 · security · Release 默认 fallback 到 cleartext http://
- **文件**:[android/app/build.gradle.kts:14](android/app/build.gradle.kts#L14)
- **证据**:`val authBaseUrl: String = localProps.getProperty("AUTH_BASE_URL", "http://10.0.2.2:8010/")`
- **风险**:release APK 默认烧进模拟器地址 + cleartext,部署到任何真实设备全失败 / 中间人
- **修复**:change default fallback for release to https URL or fail-fast at build time when `AUTH_BASE_URL` is missing/non-https;require explicit allowed-prefix check for release/acceptance.

### 3. build.gradle.kts:54 · security · Acceptance 用 debug keystore 签名
- **文件**:[android/app/build.gradle.kts:54](android/app/build.gradle.kts#L54)
- **证据**:`create("acceptance") { ... signingConfig = signingConfigs.getByName("debug") ... }`
- **风险**:生产形态构建 + 共享签名身份 → 任何人都能用 debug 私钥签名冒充
- **修复**:create `acceptanceSigningConfig` that loads from a local keystore in `local.properties` or a Gradle property;never let a non-debuggable build ship signed by the debug key.

### 4. config.py:89 · security · `dev-internal-worker-token` 入仓
- **文件**:[services/api/app/core/config.py:89](services/api/app/core/config.py#L89)
- **证据**:`internal_worker_token: SecretStr = Field(default=SecretStr("dev-internal-worker-token-change-me-123456"), min_length=32)`
- **风险**:默认 dev 值入仓,且 `scripts/accept_conference_workflow.py` 和 `tests/test_conference_workflow_acceptance.py` 复制了同一字符串
- **修复**:remove the default and require `JIANGHU_INTERNAL_WORKER_TOKEN` at startup;**rotate the worker token before any non-local use**.

### 5. LuggageViewModel.kt:137 · correctness · 副作用失败阻断主流程
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt:137](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L137)
- **证据**:`fun loadManualDetail(manualId: String) = loadDetail { repository.recordLessonRead(manualId); copy(manualDetail = repository.manualDetail(manualId)) }`
- **风险**:`recordLessonRead` 抛错会阻断 manual detail 渲染
- **修复**:wrap `repository.recordLessonRead(manualId)` in `runCatching { ... }`

### 6. AuthRepository.kt:121 · correctness · logout 跳过 clearSession
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt:121](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt#L121)
- **证据**:`suspend fun logoutCurrent() { val refreshToken = tokenStore.readRefreshToken(); try { if (refreshToken != null) api.logout(refreshToken) } finally { clearSession() } }`
- **风险**:KeyStore 读取失败时跳过 `clearSession()`,下次启动带 stale token
- **修复**:move `val refreshToken = tokenStore.readRefreshToken()` inside the `try { } finally { clearSession() }` block

### 7. AuthRepository.kt:101 · correctness · God-repository(混入 settings/feedback/blacklist/sessions)
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt:101](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt#L101)
- **证据**:行 101-118 暴露 `getSettings/updateSettings/submitFeedback/getBlacklist/removeFromBlacklist/getSessions`
- **修复**:split into `AuthRepository` / `SettingsRepository` / `BlacklistRepository` / `FeedbackRepository` / `SessionRepository` — each owns its own API surface.

### 8. CreationViewModel.kt:1170 · correctness · ViewModel 触及 ContentResolver/Uri
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/creation/CreationViewModel.kt:1170](android/app/src/main/java/com/jueqiao/jianghu/creation/CreationViewModel.kt#L1170)
- **证据**:声明 `AndroidViewModel`;`readSketch()` 行 1170-1209 直接调 `application.contentResolver.query/openInputStream`
- **修复**:move Uri → ByteArray + metadata + mime sniffing into a `CreationSketchReader` in the data layer;ViewModel should only hold a state machine that calls the reader.

### 9. LuggageRepository.kt:81 · performance · manualDetail 3 个串行 OkHttp 调用
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt:81](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L81)
- **证据**:`suspend fun manualDetail(manualId: String): ManualDetailBundle = authorized { token -> ManualDetailBundle(manual = api.getManual(...), history = api.getManualLearningHistory(...), evidence = api.getManualEvidence(...)) }`
- **风险**:3 个独立串行调用,等待 sum 而非 max
- **修复**:run the three calls concurrently inside a single `coroutineScope { val manual = async { ... }; val history = async { ... }; val evidence = async { ... } }`. **Better**:hit a single `/v1/manuals/{id}?include=history,evidence` endpoint.

### 10. middleware.py:73 · performance · RequestSizeLimitMiddleware 缓冲整个 body
- **文件**:[services/api/app/core/middleware.py:73](services/api/app/core/middleware.py#L73)
- **证据**:upload 路径下,整个 body(up to 20MB)被缓冲到 `request_messages` 列表后才转发
- **修复**:stream the body to a counting parser;for uploads,only buffer up to the configured limit and then reject without retaining bytes. Cap memory by rejecting early and discarding buffered chunks.

### 11. Gunlun2Screen.kt:44 · maintainability · gunlun1-12 屏大段重复
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/ui/screens/gunlun2/Gunlun2Screen.kt:44](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/gunlun2/Gunlun2Screen.kt#L44)
- **证据**:`Gunlun2Screen.kt`(172 行)、`Gunlun3Screen.kt`(166 行)、`Gunlun4Screen.kt`(136 行)、`Gunlun10Screen.kt`(175 行)、`Gunlun12Screen.kt`(172 行)各重复 ~10 个 `Image(...)` 块 + 同样 offset/size 坐标
- **修复**:extract `BookSlot(drawable, x, y, w, h, contentDescription, rotate)` helper or `BookRowLayout(slots: List<BookSlot>)` composable that each gunlunN screen drives from a data list.

### 12. Houshan1Screen.kt:79 · maintainability · 后山1/2/3 重复 4 个 label Box
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt:79](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt#L79)
- **证据**:Houshan1Screen.kt(249)、Houshan2Screen.kt(208)、Houshan3Screen.kt(201)重复 4 个 label Box + 相同 `Color(0xFF385816)/YaHei` 模式
- **修复**:extract `HoushanLabel(x, y, w, h, verticalText: String, fontSize, secondary: String?)` composable

### 13. LuggageViewModel.kt:62 · maintainability · 5 ViewModel 复制 try/CancellationException 样板
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt:62](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L62)
- **证据**:LuggageViewModel 行 62, 129, 201, 231, 307, 336, 364, 407 共 12+ 次;CreationViewModel 11x、AuthViewModel 6x、ConferenceViewModel 5x、DistributionViewModel 6x
- **修复**:extract `runOp(operation, fallback, block: suspend () -> T)` (or `ViewModel.launchApi` extension) helper that handles CancellationException + AuthApiException + generic Exception once.

### 14. ShengtuScreen.kt:113 · maintainability · 1824 行"上帝组件"+ 44 参数
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/ui/screens/shengtu/ShengtuScreen.kt:113](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/shengtu/ShengtuScreen.kt#L113)
- **证据**:Composable with 44 parameters including 18 `onXxx` callbacks and 13 `xxx` data parameters;JianghuNavHost.kt:112-148 wires 6 ViewModels and forwards ~40 callbacks
- **修复**:split into sub-screens (`ShengtuDraftScreen`, `ShengtuSealScreen`, `ShengtuSubmitScreen`) and pass a single `ShengtuViewModel` (or callbacks grouped into `ShengtuCallbacks` data class).

### 15. ConferenceScreens.kt:1 · maintainability · 1353 行"上帝文件"
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/ui/screens/dahui/ConferenceScreens.kt:1](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/dahui/ConferenceScreens.kt#L1)
- **证据**:单文件承载 `ConferenceHubScreen`, `ConferenceWorkScreen`, `ConferenceMatchScreen`, `ConferenceLettersScreen`, `ConferenceCollectionsScreen`, `ConferenceRequestsScreen` + 全部 previews
- **修复**:move each screen into its own file under `ui/screens/dahui/` so the package mirrors the routes.

### 16. JianghuNavHost.kt:112 · maintainability · 1470 行"上帝文件"
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt:112](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L112)
- **证据**:单 composable 接入 6 个 ViewModel + 持 login transition animator + ~50 个 `composable(Routes.X)` 块
- **修复**:split into `NavGraphs.kt` (route-to-screen map), `NavFactories.kt` (ViewModel factories per route) and `NavTransitions.kt` (LoginMistTransition wiring).

### 17. CreationViewModel.kt:417 · maintainability · 用户可见字符串硬编码在 ViewModel
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/creation/CreationViewModel.kt:417](android/app/src/main/java/com/jueqiao/jianghu/creation/CreationViewModel.kt#L417)
- **证据**:`"草图上传失败，请重新选择"` (CreationViewModel.kt:417), `"行囊暂时无法打开，请稍后重试"` (LuggageViewModel.kt:65), `"评语已提交"` (ConferenceViewModel.kt:138) 等数十条
- **修复**:move every user-facing string into `res/values/strings.xml` and resolve via `context.getString(R.string....)` so i18n is feasible.

### 18. JianghuApp.kt:14 · maintainability · 手写 DI(无 Hilt/Koin)
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/JianghuApp.kt:14](android/app/src/main/java/com/jueqiao/jianghu/JianghuApp.kt#L14)
- **证据**:`JianghuApp.kt:14-31` 手工实例化 AuthRepository/SettingsRepository/LuggageRepository;NavHost.kt:117-143 构造 5 个 ViewModelFactory;每个 ViewModel 都有自己的手写 factory
- **修复**:adopt Hilt (or Koin) with `@HiltAndroidApp` / `@AndroidEntryPoint` / `@HiltViewModel`;drop the hand-rolled factories and the imperative wiring block in `JianghuApp.onCreate`.

### 19. settings.gradle.kts:22 · architecture · 0 模块化(全在 :app 一个模块)
- **文件**:[android/settings.gradle.kts:22](android/settings.gradle.kts#L22)
- **证据**:`settings.gradle.kts` 仅声明 `include(":app")`;每个 feature 是 `:app` 的子包
- **修复**:split into `:app`, `:core`, `:feature-auth`, `:feature-luggage`, `:feature-creation`, `:feature-conference`, `:feature-settings` modules so package boundaries become enforced Gradle boundaries.

### 20. LuggageApi.kt:22 · maintainability · AuthApi + LuggageApi 复制 OkHttp/Gson/transport
- **文件**:[android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt:22](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L22)
- **证据**:LuggageApi.kt:22-32 与 AuthApi.kt:17-27 各 build 自己的 OkHttpClient,timeouts 相同;都重声明 `jsonMediaType`, root URL stripping, `parseApiError`
- **修复**:extract a shared `HttpTransport/ApiClient` that owns the OkHttpClient, request-id header, error envelope, JSON parsing, and Dispatchers.IO switching;each `*Api` class becomes a thin endpoint holder.

---

## 🟡 MEDIUM 严重度 — 36 个(摘要)

| 文件:行 | 维度 | 摘要 |
|---|---|---|
| [ConferenceViewModel.kt:384](android/app/src/main/java/com/jueqiao/jianghu/conference/ConferenceViewModel.kt#L384) | correctness | `readLetter` 后台 Job 未跟踪,`reset()` 无法取消 |
| [AuthApi.kt:224](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthApi.kt#L224) | correctness | `requestNoContentOrJson` 是 `requestNoContent` 的死重复 |
| [AuthRepository.kt:23](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt#L23) | correctness | `restoreSession` 仅在 401 时 clearSession,其他 auth 错误留下 stale token |
| [AndroidManifest.xml:38](android/app/src/main/AndroidManifest.xml#L38) | correctness | Boot receiver exported 但未 gate `RECEIVE_BOOT_COMPLETED` 权限 |
| [Home1Screen.kt:144](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/home/Home1Screen.kt#L144) | correctness | `shouldAnimateQuickActions` 在 `remember` 里无 key,后续参数变化忽略 |
| [LuggageViewModel.kt:153](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L153) | correctness | ViewModel 直接处理 Gson `JsonElement` 推导 answerSchema |
| [JianghuNavHost.kt:157](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L157) | correctness | NavHost composable 混入业务逻辑(派生列表 + notification 字符串 + 导航编排) |
| [config.py:61](services/api/app/core/config.py#L61) | correctness | 默认 fixed OTP `123456` + `noop` SMS provider on by default |
| [Home1Screen.kt:141](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/home/Home1Screen.kt#L141) | performance | `statusBarTop` 每 recomposition 重算;进场动画嵌套 launch/coroutineScope |
| [LuggageViewModel.kt:213](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L213) | performance | `refreshMistakeAfterRetry` 串行 2 个网络调用 + 一次全量 refresh |
| [ConferenceViewModel.kt:119](android/app/src/main/java/com/jueqiao/jianghu/conference/ConferenceViewModel.kt#L119) | performance | `loadWork` 串行 3 调用(work / reviews / versions) |
| [AuthApi.kt:19](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthApi.kt#L19) | performance | AuthApi + LuggageApi 各自 build OkHttpClient,连接池/线程池/DNS 不共享 |
| [LuggageViewModel.kt:395](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L395) | performance | 一个大 `LuggageDetailState`,任一字段变就 re-emit 整包 |
| [LuggageRepository.kt:319](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L319) | performance | `creationDetail` 8+ 串行 HTTP 调用,大多互不依赖 |
| [PublicationFeedCard.kt:35](android/app/src/main/java/com/jueqiao/jianghu/ui/components/PublicationFeedCard.kt#L35) | performance | `AsyncImage` 无 size hint,仅 `heightIn(min, max)` 范围 |
| [Gunlun10Screen.kt:156](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/gunlun10/Gunlun10Screen.kt#L156) | maintainability | `Color(0xFF385816)` 等 hex 在 gunlun/houshan 屏重复 |
| [JianghuNavHost.kt:1461](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L1461) | maintainability | 无 ticket 编号的 TODO 注释 |
| [ConferenceViewModel.kt:97](android/app/src/main/java/com/jueqiao/jianghu/conference/ConferenceViewModel.kt#L97) | maintainability | 多处 `catch (_: Exception)` 静默吞掉真错误 |
| [LuggageViewModel.kt:13](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L13) | maintainability | UI state 命名不统一(`LuggageUiState` / `LuggageDetailState` / `DistributionState` ...) |
| [ConferenceViewModel.kt:80](android/app/src/main/java/com/jueqiao/jianghu/conference/ConferenceViewModel.kt#L80) | maintainability | 错误→字符串映射命名不一致(`userMessage` vs `messageForUser`) |
| [ConferenceViewModel.kt:42](android/app/src/main/java/com/jueqiao/jianghu/conference/ConferenceViewModel.kt#L42) | maintainability | 状态/阶段硬编码字符串(`IDEATION` / `ENDED` / `QUEUED` / `RUNNING`) |
| [LuggageApi.kt:64](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L64) | maintainability | page-limit magic 数(20, 50)在 API + Repository 重复 |
| [LuggageViewModel.kt:85](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L85) | maintainability | 分页 `loadMore` 模式 6 处重复 |
| [LuggageRepository.kt:322](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L322) | maintainability | `runCatching 404 → null` 样板在 `creationDetail` 9 处重复 |
| [Houshan1Screen.kt:50](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/houshan1/Houshan1Screen.kt#L50) | maintainability | `StandardGunlunScaffold` 已存在,但 houshan 屏没复用 |
| [LuggageApi.kt:1085](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L1085) | maintainability | 重试策略 AuthApi(不重试)与 LuggageApi(GET 重试)不一致 |
| [LuggageApi.kt:1067](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L1067) | maintainability | AuthApi + LuggageApi 整个 request/response pipeline 复制 |
| [EncryptedTokenStore.kt:14](android/app/src/main/java/com/jueqiao/jianghu/auth/EncryptedTokenStore.kt#L14) | maintainability | `EncryptedTokenStore` + `SettingsPreferences` 均为具体类,无 interface → 难单测 |
| [LuggageApi.kt:6](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L6) | architecture | luggage 包 import auth 包的 `ApiErrorEnvelope` / `AuthApiException` — 跨 feature 依赖 |
| [Routes.kt:38](android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt#L38) | architecture | Navigation 字符串化 + `entry.arguments?.getString(...).orEmpty()` 静默回退 |
| [LuggageViewModel.kt:14](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L14) | architecture | DTO 直接泄露到 UI state(应映射为 view-model) |
| [build.gradle.kts:55](android/app/build.gradle.kts#L55) | architecture | 每 buildType 的 URL 在 build.gradle.kts 硬编码,应从 local.properties 参数化 |
| [main.py:78](services/api/app/main.py#L78) | architecture | 后端无 DI 框架,services stash 在 `application.state` + 每次 Depends 重新解析 |
| [SettingsRepository.kt:14](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/settings/SettingsRepository.kt#L14) | architecture | `SettingsRepository` 多关注点 dump(settings + sessions + blacklist + feedback + account refresh) |
| [build.gradle.kts:50](android/app/build.gradle.kts#L50) | security | acceptance build 用 debug signing key + cleartext URL |
| [config.py:200](services/api/app/core/config.py#L200) | security | MinIO TLS flags 默认 False;生产 validator 未强制 TLS |
| [LuggageRepository.kt:151](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L151) | performance | `learnedManualSources` 3 个串行 `/v1/manuals` 调用 + 内存去重 |

---

## ⚪ LOW 严重度 — 37 个(摘要)

| 文件:行 | 维度 | 摘要 |
|---|---|---|
| [EncryptedTokenStore.kt:57](android/app/src/main/java/com/jueqiao/jianghu/auth/EncryptedTokenStore.kt#L57) | correctness | `getOrCreateKey` 漏 KeyStore/Generator 异常未恢复 |
| [AuthViewModel.kt:37](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthViewModel.kt#L37) | correctness | `clearFeedback` 用不一致的 guard |
| [SettingsRepository.kt:39](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/settings/SettingsRepository.kt#L39) | correctness | `setFloat` 绕过 `setBoolean` 的通知重排路径 |
| [AndroidManifest.xml:10](android/app/src/main/AndroidManifest.xml#L10) | correctness | `android:allowBackup=true` 暴露 `jianghu_user_settings` |
| [libs.versions.toml:1](android/gradle/libs.versions.toml#L1) | correctness | 无 biometric 集成 / 无 deep link |
| [security.py:110](services/api/app/core/security.py#L110) | correctness | access-token denylist 缺失,`logoutAll` 无法踢被盗 access token |
| [auth.py:387](services/api/app/services/auth.py#L387) | correctness | refresh endpoint 无独立 rate limit |
| [auth.py:323](services/api/app/services/auth.py#L323) | correctness | login dummy-burn mitigation 健全 — 仅确认 |
| [AuthApi.kt:216](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthApi.kt#L216) | performance | no-content 助手 buffer 整个 body 即便丢弃 |
| [JianghuNavHost.kt:697](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L697) | performance | `Gongfang` composable 每次 recomposition 重算 `recentWorks`/`analysisNotice` |
| [JianghuNavHost.kt:157](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L157) | performance | `startLoginTransition` lambda 每次 recomposition 重新分配 |
| [LuggageScreen.kt:544](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/home/LuggageScreen.kt#L544) | performance | `ManualsCard.visibleItems` filter list 每次重算 |
| [LuggageScreen.kt:581](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/home/LuggageScreen.kt#L581) | performance | `ManualFilters` filter list 每次重算 |
| [LuggageScreen.kt:509](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/home/LuggageScreen.kt#L509) | performance | `GrowthSeal.flecks` Pair list 在 @Composable 内每次重分配 |
| [SettingsPreferences.kt:37](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/settings/SettingsPreferences.kt#L37) | maintainability | `snapshots` 每次 SharedPreferences 变化全 12 pref 重读 |
| [LuggageViewModel.kt:94](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L94) | performance | 无 in-flight `loadManuals`/`loadCreations`/`loadMistakes` 去重 |
| [LuggageRepository.kt:319](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L319) | maintainability | 大部分 public Repository/ViewModel 函数缺 KDoc |
| [LoginScreen.kt:258](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/login/LoginScreen.kt#L258) | maintainability | 412x900 preview 注释复制 |
| [LuggageRepository.kt:596](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L596) | maintainability | 3 个 Repository 各自复制 `authorized { token -> ... }` |
| [LuggageViewModel.kt:154](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L154) | maintainability | answer-payload property 查询在 trial + retry 重复 |
| [JianghuApp.kt:21](android/app/src/main/java/com/jueqiao/jianghu/JianghuApp.kt#L21) | performance | `onCreate` 在主线程同步 SharedPreferences 磁盘 I/O |
| [Home1Screen.kt:422](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/home/Home1Screen.kt#L422) | performance | `DecorButton.lift.toPx()` via `with(density)` 每次 recompute |
| [JianghuNavHost.kt:144](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L144) | maintainability | `rememberCoroutineScope` 用作导航编排 — 反模式 |
| [Validators.kt:14](android/app/src/main/java/com/jueqiao/jianghu/data/Validators.kt#L14) | security | password 校验接受 8–64 字符的全空白密码 |
| [AuthRepository.kt:23](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt#L23) | security | `restoreSession` 仅在 401 时清本地状态 |
| [AndroidManifest.xml:34](android/app/src/main/AndroidManifest.xml#L34) | security | Reminder receiver 已锁 — 仅审计确认 |
| [LuggageApi.kt:1084](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L1084) | security | 失败时日志打印含 query string 的完整 URL |
| [AuthApi.kt:205](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthApi.kt#L205) | security | 服务端错误路径在 release 构建用 Log.e |
| [config.py:12](services/api/app/core/config.py#L12) | security | dev 密钥硬编码为 Python 模块级常量 |
| [config.py:64](services/api/app/core/config.py#L64) | security | in-memory media storage 接受为默认(无租户隔离) |
| [Validators.kt:14](android/app/src/main/java/com/jueqiao/jianghu/data/Validators.kt#L14) | maintainability | Validators 边界用例测试覆盖弱 |
| [ShengtuScreen.kt:162](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/shengtu/ShengtuScreen.kt#L162) | maintainability | 生产屏代码里有 debug-only `Log.d` |
| [ConferenceViewModel.kt:122](android/app/src/main/java/com/jueqiao/jianghu/conference/ConferenceViewModel.kt#L122) | performance | `loadWork` owner-only versions 调用串行 |
| [middleware.py:58](services/api/app/core/middleware.py#L58) | performance | `RequestSizeLimitMiddleware` 每次请求 build lowercase header dict |
| [config.py:117](services/api/app/core/config.py#L117) | performance | `parse_allowed_hosts` 用 `json.loads` — 因 `@lru_cache` 可接受 |
| [main.py:160](services/api/app/main.py#L160) | architecture | 无 CORS middleware — 隐式 no-CORS |
| [main.py:156](services/api/app/main.py#L156) | architecture | 无 HSTS / Permissions-Policy header |
| [main.py:145](services/api/app/main.py#L145) | architecture | FastAPI middleware `add_order` vs `execution_order` 混乱 |
| [HomeScreen.kt:118](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/home/HomeScreen.kt#L118) | maintainability | "BannerRow removed" 陈旧注释 |
| [JianghuNavHost.kt:116](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L116) | maintainability | `JianghuApp.lateinit` service-locator 反模式 |
| [CreationViewModel.kt:1230](android/app/src/main/java/com/jueqiao/jianghu/creation/CreationViewModel.kt#L1230) | maintainability | 状态集(`IMAGE_GENERATION_ACTIVE` / `EXPORT_ACTIVE`)藏为私有常量 |
| [proguard-rules.pro:1](android/app/proguard-rules.pro#L1) | maintainability | ProGuard 规则极少 + `isMinifyEnabled=true` + Gson-reflected DTO → 字段剥离风险 |
| [app/api/routes](services/api/app/api/routes) | architecture | 后端路由模块扁平(17 文件),与 `app/domains/` 树状分组不一致 |

> 表格含 42 条,实际 LOW 数 37 — 多余 5 条系分类合并或 verify 失败后去重。请以本节总数 37 为准。

---

## 🎯 Top 8 优先行动(执行顺序)

| # | 文件:行 | 维度 | 操作 |
|---|---|---|---|
| 1 | [LuggageApi.kt:303](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L303) | security | reject any uploadUrl that is not https and whose host is not in an allow-list(or always proxy through `/v1/uploads/{id}/object` with bearer token) |
| 2 | [AuthRepository.kt:121](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt#L121) | correctness | move `val refreshToken = tokenStore.readRefreshToken()` inside the `try { } finally { clearSession() }` block |
| 3 | [LuggageViewModel.kt:137](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L137) | correctness | wrap `repository.recordLessonRead(manualId)` in `runCatching { ... }` |
| 4 | [LuggageRepository.kt:81](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L81) | performance | parallelize 3 sequential calls via `coroutineScope { async {} }.awaitAll()`;also fix `:151` learnedManualSources + `:319` creationDetail |
| 5 | [build.gradle.kts:14](android/app/build.gradle.kts#L14) + [:54](android/app/build.gradle.kts#L54) | security | force https for release + create non-debug acceptanceSigningConfig |
| 6 | [config.py:89](services/api/app/core/config.py#L89) | security | rotate `JIANGHU_INTERNAL_WORKER_TOKEN` and remove the default;also fix duplicates in `scripts/accept_conference_workflow.py` + `tests/test_conference_workflow_acceptance.py` |
| 7 | [JianghuApp.kt:14](android/app/src/main/java/com/jueqiao/jianghu/JianghuApp.kt#L14) + [JianghuNavHost.kt:117-143](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L117-L143) | maintainability | adopt Hilt (or Koin) and replace hand-rolled DI + 5 ViewModelFactory |
| 8 | [LuggageViewModel.kt:62](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L62) + 5 ViewModels | maintainability | extract `runOp` / `launchApi` helper — centralize CancellationException + AuthApiException + generic Exception |

---

## ⚠️ 验证缺口

22 个 verifier 因 API 529(`当前服务集群负载较高`)失败,集中在 security/correctness/performance/maintainability 的 verify + cross 阶段。

| 失败数 | 类别 |
|---|---|
| 7 | security(verify) |
| 5 | performance(verify + cross) |
| 4 | correctness(verify) |
| 4 | maintainability(verify + cross) |
| 2 | architecture(cross) |

**影响**:HIGH 级别以下的部分 finding 缺少对抗复核 → 置信度比 HIGH 级弱。HIGH 级的 20 个 finding 因双 verifier 多数通过(`realVotes >= 1`),已确认。

**补救**:用 `Workflow({scriptPath: <cached>, resumeFromRunId: "wf_d6c471cd-c38"})` 重跑仅失败的 verifier,缓存已完成的 scout / scan / 已成功 verify 直接复用。

---

## 重跑与扩展

### 重跑失败 verifier

```bash
# 1. 找到 cached script path(来自上次 Workflow 工具返回值)
#    通常在: C:/Users/28784/.claude/projects/D--Appproject/.../workflows/scripts/appproject-code-scan-wf_d6c471cd-c38.js
# 2. 重跑
# Workflow({scriptPath: "<path>", resumeFromRunId: "wf_d6c471cd-c38"})
```

### 新扫描(下次扫)

将 `meta.name` 改为 `appproject-code-scan-v2`(避免 cache 命中),并考虑:
- 增加 KDoc 覆盖维度
- 增加 API docstring 一致性维度
- 加入 i18n 字符串覆盖率
- 拆分 Scout 的 highRisk 文件清单,让每个 dimension agent 真正并行不重复

---

## 下次会话可立即执行(本审计推荐)

1. **Android Studio Sync → Build → Run** 学习2 页面验证视觉(当时会话日志可用 `git show b5d2c8c:docs/SESSION-LOG-2026-09-09.md` 查看)
2. **修 HIGH #1**(SSRF):LuggageApi.kt:303 + 加 allow-list
3. **修 HIGH #2**(cleartext):build.gradle.kts:14/54
4. **修 HIGH #5/6**(会话清理):LuggageViewModel.kt:137 + AuthRepository.kt:121
5. **重跑 22 个失败 verifier**(确认 medium/low 置信度)
6. **按本审计 §Top 8 优先行动** 顺序逐项修复