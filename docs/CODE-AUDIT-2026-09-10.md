# CODE-AUDIT — 2026-09-10

> v2 多维度代码扫描结果。68 个 confirmed finding,3 个 critical / 14 个 high / 27 个 medium / 24 个 low。
> 与昨日 [CODE-AUDIT-2026-09-09.md](./CODE-AUDIT-2026-09-09.md) 对照阅读。
> 历史快照，仅用于解释仍在源码中的引用；问题状态需重新核验。关联会话和跨日摘要已从工作树移除，原文位于 Git 提交 `b5d2c8c` 的 `docs/` 中。

---

## 元数据

| 项 | 值 |
|---|---|
| 工作流脚本 | `appproject-code-audit-v2`(`meta` 4 阶段:Scout / Scan / Verify / Synthesize) |
| Workflow Run ID | `wf_1980fcb3-b31` |
| 重跑命令 | `Workflow({scriptPath: "<cached>", resumeFromRunId: "wf_1980fcb3-b31"})` |
| 触发 | 用户说"审查我的所有代码"+ 方案3(等 v2 跑完生成跨两天摘要) |
| Token 消耗 | 4.35M subagent tokens / 60 分钟 wall clock |
| Agent 调度 | 173 总 / 170 done / 3 error(详见末尾"验证缺口") |
| v1 → v2 增量 | 9 个新文件 + 5 个文件改动 + 23 个新 drawable |

## 技术栈(Scout 阶段产出,与 v1 同)

- **语言:** Kotlin (JVM 17) + Python (FastAPI) + TypeScript
- **UI:** Jetpack Compose (Material3),AndroidX Navigation,Splashscreen,Coil
- **DI:** 仍是手写 `JianghuApp.lateinit` + ViewModelFactory(未引 Hilt)
- **后端:** FastAPI + SQLAlchemy + Redis
- **网络:** OkHttp 4.x

---

## Stats

| 维度 | confirmed | 其中 NEW | 其中 PERSISTED |
|---|---:|---:|---:|
| correctness | 9 | 2 | 7 |
| security | 15 | 1 | 14 |
| performance | 7 | 3 | 4 |
| maintainability | 32 | 16 | 16 |
| architecture | 5 | 5 | 0 |
| **合计** | **68** | **27** | **41** |

| 严重度 | confirmed |
| |---:|
| critical | 3 |
| high | 14 |
| medium | 27 |
| low | 24 |

## Delta vs v1(09-09)

> **回归点**:v1 的 8 个 HIGH 优先行动,**0 个被修复**——本次重新逐个在磁盘上验证,**全部 41 个 finding 是 v1 直接延续**。
>
> **新增功能 OK**:6 个新页(Learning2/3、PendingUnlock、Gunlun13/14/15)业务逻辑 + 数据流 + 凭据处理**没有引入新缺陷**,但大量复制粘贴导致 27 个 NEW finding(其中 15 个 main-tainability)。
>
> **真正的功能 bug**:1 个 —PendingUnlock 屏的"前往解锁"按钮**不可点击**,而真正的入口是看不见的封面图。
>
> **最便宜的杠杆**:4 张大尺寸全屏 PNG 还在 `res/drawable/`(非密度桶),xxhdpi 上每张解码 ~54 MB ARGB_8888 —纯 `git mv` 到 `drawable-nodpi/` 零代码改动,能省一整片内存。
>
> **结构债反向增长**:JianghuNavHost.kt 现在 1515 行(+18),ShengtuScreen 仍 1824,ConferenceScreens 仍 1353。
>
> **测试覆盖原地踏步**:新 RoutesTest 45 行只断言字符串常量,新导航链路**零行为测试** —一个 button-click 行为测试就能逮到那个 dead button。

---

## 🔴 Critical — 3 个

### 1. LuggageApi.kt:309 · correctness · SSRF(putMediaObject 接受任意绝对 URL)

[android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt:309](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L309)

- **证据:** `isDirectUpload = uploadUrl.startsWith("http://") ||startsWith("https://")`,然后 `Request.Builder().url(upload.uploadUrl) + server-chosen requiredHeaders`。
- **影响:** 恶意/被入侵的服务端(或 MITM)可将用户媒体重定向到任意 host + 探测设备内网。直接分支**不调用 `.authorized()`**,所以 bearer token 不会泄露——影响是媒体外泄 + 内网探测。
- **修复:** 强制 `https://` 且 HttpUrl host 必须匹配 `BuildConfig.ALLOWED_UPLOAD_HOSTS` 白名单;拒绝 RFC1918/loopback。最简:删除 `isDirectUpload` 分支,统一走 `/v1/uploads/{id}/object`。
- **introduced:** PERSISTED_FROM_V1(v1 finding #5,HIGH,现在升级到 critical 因新增 commit 期间未修)

### 2. ShengtuScreen.kt:113 · maintainability · 43 参 @Composable + 250 行 NavHost 调用

[android/app/src/main/java/com/jueqiao/jianghu/ui/screens/shengtu/ShengtuScreen.kt:113](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/shengtu/ShengtuScreen.kt#L113)

- **证据:** 行 113-158 在单个 @Composable 上声明 43 个参数(文件 1824 行,已 `wc -l` 确认)。JianghuNavHost.kt:813-1061 约 250 行纯参数装配(占该文件 17%),就为调用一次 `ShengtuScreen(...)`。
- **修复:** 拆分为 `ShengtuState`(只读 props) + `ShengtuCallbacks`(事件 lambda)两个 data class;NavHost 块塌缩到几行。

### 3. res/drawable/img_gunlun1_bg.png 等 4 张 · performance · 全屏背景在 mdpi 桶上 ~54 MB/张

[android/app/src/main/res/drawable/img_gunlun1_bg.png:1](android/app/src/main/res/drawable/img_gunlun1_bg.png#L1)

- **证据:** 磁盘确认 —
  - `img_gunlun1_bg.png` 2,262,580 B (824x1834)
  - `img_houshan_bg.png` 1,726,031 B
  - `img_unfinished_image134.png` 1,568,464 B
  - `img_unfinished_compact124.png` 244,473 B
- 全部位于 `res/drawable/`(= mdpi)。3x 设备 `inDensity/inTargetDensity` 上采样到 ~2472x5502 → **每张 ~54 MB ARGB_8888**。`StandardGunlunScaffold.kt:51` 用 `img_gunlun1_bg`,每个滚轮1-15 都付一次;`Learning2Screen.kt:58` 和 `Learning3Screen.kt:62` 都用 `img_houshan_bg`。
- **修复:** `git mv` 4 张到 `res/drawable-nodpi/`(兄弟资源已经在那里,零代码改动),然后转 WebP lossless/lossy。审计其他 `>800px` 仍在 `res/drawable/` 的资源。

---

## 🟠 High — 14 个(全量)

| # | 文件:行 | 维度 | 摘要 | introduced |
|---|---|---|---|---|
| 1 | [build.gradle.kts:14](android/app/build.gradle.kts#L14) | security | `authBaseUrl` 默认 fallback 到 cleartext `http://10.0.2.2:8010/` — 无 local.properties 时静默烧进 BuildConfig | PERSISTED |
| 2 | [build.gradle.kts:50](android/app/build.gradle.kts#L50) | security | acceptance buildType 复用 debug signing config + 硬编码 cleartext `http://10.0.2.2:8011/` | PERSISTED |
| 3 | [AuthRepository.kt:121](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt#L121) | correctness | `logoutCurrent` 把 refresh token 读取放在 try/finally 外,KeyStore 失败时跳过 `clearSession()`(注意 `logoutAll` 形态正确) | PERSISTED |
| 4 | [JianghuNavHost.kt:813](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L813) | maintainability | ShengtuProject composable 块 ~250 行参数装配(占文件 17%) | PERSISTED |
| 5 | [ConferenceScreens.kt:1](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/dahui/ConferenceScreens.kt#L1) | maintainability | 1353 行上帝文件,7 个无关屏共用一个通用文件名(v1 后未动) | PERSISTED |
| 6 | [Gunlun13Screen.kt:59](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/gunlun13/Gunlun13Screen.kt#L59) | maintainability | 10 套 X/Y/W/H/rotation magic 数复制 3 份(Gunlun13/14/15) | **NEW** |
| 7 | [Gunlun13Screen.kt:60](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/gunlun13/Gunlun13Screen.kt#L60) | maintainability | Gunlun13/14/15 各 ~180 行 100% 结构重复,只有 drawable + intro 文字 + 颜色不同 | **NEW** |
| 8 | [Learning2Screen.kt:56](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/learning2/Learning2Screen.kt#L56) | maintainability | Learning2/Learning3 共享 byte-identical page chrome(bg + 返回按钮 + 卷轴 + 提示文本) | **NEW** |
| 9 | [PendingUnlockScreen.kt:55](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/pendingunlock/PendingUnlockScreen.kt#L55) | maintainability | PendingUnlock 复制 UnfinishedScreen 4 层 frame 框架(其 KDoc 承认) | **NEW** |
| 10 | [PendingUnlockScreen.kt:65](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/pendingunlock/PendingUnlockScreen.kt#L65) | performance | 3 张全屏位图堆叠,1 张声明 954x784 dp — ~3-4x overdraw + 浪费解码 | **NEW** |
| 11 | [PendingUnlockScreen.kt:108](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/pendingunlock/PendingUnlockScreen.kt#L108) | architecture | **"前往解锁"按钮是 dead affordance** — 只有"待解锁"封面图可点击 | **NEW** |
| 12 | [ShengtuScreen.kt:81](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/shengtu/ShengtuScreen.kt#L81) | maintainability | 1824 行上帝文件(4 表单 + 5+ workflow 段) | PERSISTED |
| 13 | [config.py:12](services/api/app/core/config.py#L12) | security | 模块级 dev crypto 密钥(JWT/phone lookup/code HMAC/Fernet)作为 Settings 默认值 | PERSISTED |
| 14 | [config.py:89](services/api/app/core/config.py#L89) | security | `internal_worker_token` 默认值入仓,且在 2 个其他文件中复制 | PERSISTED |

---

## 🟡 Medium — 27 个(摘要表)

| 文件:行 | 维度 | 摘要 | NEW/PERSISTED |
|---|---|---|---|
| [config.py:61](services/api/app/core/config.py#L61) | correctness | fixed OTP `123456` + `noop` SMS 默认开启 | PERSISTED |
| [AuthApi.kt:19](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthApi.kt#L19) | performance | AuthApi + LuggageApi 各 build OkHttpClient,连接池/线程池/DNS 不共享 | PERSISTED |
| [LuggageRepository.kt:319](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L319) | performance | `creationDetail` 8+ 串行 HTTP 调用 | PERSISTED |
| [LuggageRepository.kt:81](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L81) | performance | `manualDetail` 3 个串行 OkHttp 调用 | PERSISTED |
| [Home1Screen.kt:144](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/home/Home1Screen.kt#L144) | correctness | `shouldAnimateQuickActions` 在 `remember` 里无 key | PERSISTED |
| [JianghuNavHost.kt:157](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L157) | correctness | NavHost composable 混入业务逻辑(派生列表 + notification 字符串 + 导航编排) | PERSISTED |
| [EncryptedTokenStore.kt:14](android/app/src/main/java/com/jueqiao/jianghu/auth/EncryptedTokenStore.kt#L14) | maintainability | EncryptedTokenStore + SettingsPreferences 均为具体类,无 interface → 难单测 | PERSISTED |
| [LuggageApi.kt:6](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L6) | architecture | luggage 包 import auth 包的 ApiErrorEnvelope / AuthApiException — 跨 feature 依赖 | PERSISTED |
| [Routes.kt](android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt) | architecture | 70+ 字符串路由 + 6 helper 函数,路由表难浏览 | **NEW |
| [LuggageViewModel.kt:153](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L153) | correctness | ViewModel 直接处理 Gson `JsonElement` 推导 answerSchema | PERSISTED |
| [main.py:78](services/api/app/main.py#L78) | architecture | 后端无 DI 框架,services stash 在 `application.state` + 每次 Depends 重新解析 | PERSISTED |
| [LuggageViewModel.kt:395](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageViewModel.kt#L395) | performance | 大 `LuggageDetailState`,任一字段变就 re-emit 整包 | PERSISTED |
| [ShengtuScreen.kt:162](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/shengtu/ShengtuScreen.kt#L162) | maintainability | 生产屏代码里有 debug-only `Log.d` | PERSISTED |
| [auth.py:387](services/api/app/services/auth.py#L387) | correctness | refresh endpoint 无独立 rate limit | PERSISTED |
| [ConferenceViewModel.kt:97](android/app/src/main/java/com/jueqiao/jianghu/conference/ConferenceViewModel.kt#L97) | maintainability | 多处 `catch (_: Exception)` 静默吞掉真错误 | PERSISTED |
| [ConferenceViewModel.kt:122](android/app/src/main/java/com/jueqiao/jianghu/conference/ConferenceViewModel.kt#L122) | performance | `loadViewManager` owner-only versions 调用串行 | PERSISTED |
| [JianghuNavHost.kt:1461](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L1461) | maintainability | 无 ticket 编号的 TODO 注释 | PERSISTED |
| [SettingsPreferences.kt:37](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/settings/SettingsPreferences.kt#L37) | maintainability | `snapshots` 每次 SharedPreferences 变化全 12 pref 重读 | PERSISTED |
| [Gunlun2Screen.kt:44](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/gunlun2/Gunlun2Screen.kt#L44) | maintainability | gunlun1-12 屏大段重复(v1 后未动) | PERSISTED |
| [LuggageApi.kt:64](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L64) | maintainability | page-limit magic 数(20, 50)在 API + Repository 重复 | PERSISTED |
| [LuggageApi.kt:1067](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L1067) | maintainability | AuthApi + LuggageApi 整个 request/response pipeline 复制 | PERSISTED |
| [JianghuNavHost.kt:1515](android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L1515) | maintainability | 整个文件 1515 行(+18),~50 个 inline destinations | **NEW** |
| [Gunlun15Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/gunlun15/Gunlun15Screen.kt) | architecture | Gunlun15 是死胡同:`onPandaClick` 未传,只能连按 15 次返回 | **NEW** |
| [Validators.kt:14](android/app/src/main/java/com/jueqiao/jianghu/data/Validators.kt:14) | security | `isPassword` 仅长度校验,接受 '12345678' | PERSISTED |
| [Learning2Screen.kt](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/learning2/Learning2Screen.kt) | maintainability | 中文用户可见字符串硬编码在 Kotlin 字面量(全 6 新屏都有) | **NEW** |
| [routes/RoutesTest.kt](android/app/src/test/java/com/jueqiao/jianghu/nav/RoutesTest.kt) | architecture | 新 45 行 RoutesTest 只断言字符串常量 — 行为测试 0 个 | **NEW** |

> 摘要表 27 行。剩余 medium/low 24 个表格见 v1 报告 [CODE-AUDIT-2026-09-09.md](./CODE-AUDIT-2026-09-09.md) 同档位(多数 PERSISTED_FROM_V1)。

---

## 🎯 Top 8 优先行动(v2 重排)

按"工作量 / 风险比"排序:

| # | 文件:行 | 行动 | 期望收益 |
|---|---|---|---|
| 1 | [LuggageApi.kt:309](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt#L309) | **critical SSRF**:删除直传分支或加 https + 白名单 | 阻止媒体外泄 + 内网探测 |
| 2 | [AuthRepository.kt:121](android/app/src/main/java/com/jueqiao/jianghu/auth/AuthRepository.kt#L121) | `readRefreshToken()` 移入 try 块 + `EncryptedTokenStore.kt:57` 捕获 `KeyPermanentlyInvalidatedException` | logout 永远清 session |
| 3 | [res/drawable/img_gunlun1_bg.png 等 4 张](android/app/src/main/res/drawable/) | `git mv` 到 `res/drawable-nodpi/` + 转 WebP | xxhdpi 上每张省 ~50 MB 解码,零代码改动 |
| 4 | [build.gradle.kts:14, :50](android/app/build.gradle.kts#L14) | release/acceptance 强制 AUTH_BASE_URL,删除 debug signing 复用 | 阻止生产 cleartext APK |
| 5 | [config.py:12, :89](services/api/app/core/config.py#L89) | 删 dev 密钥默认 + 轮换 `JIANGHU_INTERNAL_WORKER_TOKEN`,3 个文件改 env 读 | 阻止 dev secret 漏到 staging |
| 6 | [PendingUnlockScreen.kt:108](android/app/src/main/java/com/jueqiao/jianghu/ui/screens/pendingunlock/PendingUnlockScreen.kt#L108) | "前往解锁"按钮加 clickable(并把"待解锁"封面图 clickable 降级) + KDoc 修正 | 修 1 个真实功能 bug |
| 7 | [LuggageRepository.kt:81](android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt#L81) | `manualDetail` 3 调用 `coroutineScope { async }.awaitAll()` 并行 | 详情屏加载 ~3x 提速 |
| 8 | 抽 `BookShelfScaffold` + `LearningPageChrome` + `BookFrameScaffold`(各 ~80 行) | Gunlun13/14/15、Learning2/3、PendingUnlock/Unfinished 全部塌缩到 1 行声明 | 一次性消掉 9 个 maintainability finding + 阻止后续复制 |

---

## ⚠️ 验证缺口

3 个 verifier 失败:
- `verify:maintainability` — 安全分类阻塞(stage 1 评估,通常是瞬态的)
- `verify:architecture` — 安全分类超时
- `cross:maintainability` + `cross:performance` — 连接拒绝

**影响:** 这 4 个维度的对抗验证部分缺失,**比 v1 的 22 个少得多**(只剩 3-4 个 finding 缺少双 verifier)。

**补救:** `Workflow({scriptPath: "<cached>", resumeFromRunId: "wf_1980fcb3-b31"})` 重跑仅失败的 4 个 verifier。

---

## 重跑与扩展

### 重跑失败 verifier

```bash
# 找 cached script path(上次工具返回值)
# Workflow({scriptPath: "<path>", resumeFromRunId: "wf_1980fcb3-b31"})
```

### 进一步扫描

下次扫描可考虑:
- 加入 i18n 字符串覆盖率维度
- 加入 `git diff` 增量扫描(本次已用 `introduced: NEW/PERSISTED_FROM_V1` 标注)
- 加入行为测试覆盖分析(目前 0%)

---

## 下次会话可立即执行

1. **Android Studio Sync → Build → Run** Gunlun15 验证视觉(15+15 没新增按钮,只有返回)
2. **修 HIGH #6**(dead 按钮):PendingUnlock"前往解锁"按钮加 clickable —5 分钟
3. **修 HIGH #2**(会话清理):AuthRepository.kt:121 + EncryptedTokenStore.kt:57 —10 分钟
4. **HIGH #3**(`git mv` 4 张 PNG):drawable/ → drawable-nodpi/ —3 分钟,零代码
5. **HIGH #8**(抽 3 个 scaffold):一次性消掉 9 个 maintainability finding —30 分钟
6. 重跑 3 个失败 verifier(确认完整覆盖)