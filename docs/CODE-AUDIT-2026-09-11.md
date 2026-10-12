# CODE-AUDIT — 2026-09-11 (Vol-2 注释对齐批 + 周边屏幕)

> 历史快照，仅用于解释源码引用；以下问题和结论均属于当时检查，不代表当前状态。

> v3 代码审计快照。覆盖刚合并到 main 的 `b04d25f`(Vol-2-10~15 注释对齐 + 8e5cded doc-only commit + ac6a168 refactor + 上一批次 Vol-2-7/8/9/Vol-1-14/catch-up batch Vol-2-13/14/15/Vol-3-1)。8 个评审角度并行(line-by-line diff / removed-behavior / cross-file tracer / efficiency+altitude / reuse+simplification / language-pitfall / wrapper-proxy / conventions) + 2 个 verify pass。**核心结论:Vol-2 doc 批本身无 runtime bug,但暴露出 5+ 处 Vol-2-7/8/9 的 pre-existing KDoc/注释漂移,且项目最严重的 9-screen copy-paste 问题未在 `ac6a168` "refactor" commit 中得到解决**。

---

## 元数据

| 项 | 值 |
|---|---|
| 审计范围 | merge `b04d25f`(zzz → main,2 commits: 8e5cded doc + ac6a168 refactor)|
| 受影响文件 | 16 个 Kotlin + 6 个 PNG + 1 个 SESSION-LOG |
| 评审角度 | 8 个并行 agent + 2 个 verify pass |
| 评审方式 | line-by-line diff + 全文读 + cross-file grep + Routes.kt / JianghuNavHost.kt 对照 |
| 编译状态 | `compileDebugKotlin` + `testDebugUnitTest` BUILD SUCCESSFUL(纯注释改动)|
| 本次 introduced 缺陷 | **0 runtime bug**;注释/KDoc drift 若干 |
| 暴露 pre-existing 缺陷 | **🔴 HIGH 9 项 / 🟡 MEDIUM 6 项 / ⚪ LOW 9 项** |

---

## Stats

| 严重度 | 数量 | 说明 |
|---|---|---|
| 🔴 HIGH(功能/正确性)| 9 | 含 Vol-2-7 错图号、duplicate composable、9-screen copy-paste 等 |
| 🟡 MEDIUM(文档漂移/约定)| 6 | 交替模式列表、KDoc 不一致、callback 命名不一 |
| ⚪ LOW(优化/小改进)| 9 | 重构机会、remember 缺失、a11y 等 |

---

## 🔴 HIGH 全量(9 项)

### H1. Vol-2-7 行内注释引用错误的 image 编号 🔴🔴
- **文件**: [Volume2Part7Screen.kt:111](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part7/Volume2Part7Screen.kt#L111)
- **问题**: 行内注释 `// 图2(image 289.png,X=24, Y=381, W=356, H=214)` 但 [line 120](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part7/Volume2Part7Screen.kt#L120) `painterResource(R.drawable.img_volume2part7_image_292)` 加载的是 image **292** — 注释里写的 289 是 Vol-2-8 在用的图
- **背景**: Vol-2-7 历史上用过 289,被 Vol-2-7 用 292 替换后腾出来给 Vol-2-8 复用(Vol-2-8 KDoc L41 记载)
- **影响**: 维护者读注释以为 Vol-2-7 用 289,可能误删 289 资源或"按注释修复"反而引入正确性的回退

### H2. Vol-2-7 KDoc L34, L35 全面 stale
- **文件**: [Volume2Part7Screen.kt:34-35](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part7/Volume2Part7Screen.kt#L34)
- **问题**: 
  - L34 KDoc 图1:`X=28, Y=155, W=352, H=203` → 实际 [L100-101](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part7/Volume2Part7Screen.kt#L100) `offset(18, 135).size(352, 283)` — X/Y/H 全错
  - L35 KDoc 图2:`X=24, Y=381, W=356, H=214` → 实际 [L116-117](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part7/Volume2Part7Screen.kt#L116) `offset(28, 481).size(340, 254)` — 4 值全错
- **影响**: "design-by-comment" 失效 — 后续按 KDoc 还原布局会回到错误坐标

### H3. Vol-2-8 KDoc + 行内注释 stale (4 处)
- **文件**: [Volume2Part8Screen.kt:34-35, 96, 111](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part8/Volume2Part8Screen.kt#L34)
- **问题**: 
  - L34 KDoc 图1:`X=28, Y=155, W=352, H=203` → 实际 `offset(24, 135).size(352, 295)`
  - L35 KDoc 图2:`X=24, Y=381, W=356, H=214` → 实际 `offset(24, 451).size(356, 290)`
  - L96 + L111 行内注释同 stale
- **影响**: 同 H2

### H4. Vol-2-9 行内注释 + KDoc 部分 stale(只 W/H 错,X/Y 对)
- **文件**: [Volume2Part9Screen.kt:34, 94](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part9/Volume2Part9Screen.kt#L34)
- **问题**: 
  - L94 行内注释:`W=352, H=203` → 实际 `size(332, 237)` — **20dp 窄,34dp 高**
  - L34 KDoc 同 stale
- **影响**: image_293 实际渲染 20dp 窄 / 34dp 高 — `ContentScale.FillBounds` 会强制拉伸,可能视觉裁剪/letterbox。设计稿 W=352 H=203 与实际 W=332 H=237 不匹配,可能是早期 prototype 调整未同步

### H5. `JianghuNavHost.kt` duplicate `composable(Routes.Learning3)` 块 🔴🔴
- **文件**: [JianghuNavHost.kt:475-479 AND 484-489](android/app/srcrc/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt#L475)
- **问题**: 两个 block 字节完全相同(同样的 `onBack` + `onOpenPendingUnlock`)。Compose Navigation 对同一 route 重复注册第二个会触发 "Duplicate destinations" 警告/错误
- **影响**: 取决于框架行为 — 要么后注册的 shadow 前者(死代码),要么前者 shadow 后者;任一情况未来对其中一处的修改只会影响一半路由
- **状态**: pre-existing(不在本次合并范围),但严重度高,建议单独修

### H6. 9-screen copy-paste 无共享 `BookFrameScaffold` / `VolumePageScaffold`
- **范围**: Vol-2-7/8/9/10/11/12/13/14/15(共 9 屏)+ Vol-1-14(10 屏)
- **问题**: 全部 ~127 行,代码结构字节级相同,仅 5 字段不同(title 文本、book-frame drawable、2 image drawables、4 image (X,Y,W,H) 元组、callback 名)
- **现状**:
  - `ui/components/` 目录**不存在** `BookFrameScaffold.kt` / `VolumePage.kt`
  - `PendingUnlockScreen.kt:41` 的 KDoc 自己**已经建议**提取 `BookFrameScaffold`
  - commit `ac6a168` 标题 "Refactor code structure for improved readability and maintainability" **未提取任何抽象**(纯新增 3 屏 + 6 PNG + 5 坐标微调)— 标题是误导
- **影响**: 本次审计的"comment-vs-code drift"问题正是这模式的具体症状 — Vol-2-7/8/9 stale KDoc 就是因为手工复制时漏改注释。Bug fix 必须 N 处同步,新屏依然继续 copy-paste

### H7. Vol-2-14 book-frame 与既有交替模式规则冲突 🔴🔴
- **文件**: [Volume2Part14Screen.kt:71](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part14/Volume2Part14Screen.kt#L71)
- **规则** (Vol-2-11/12 KDoc L32 权威版本): `Vol-2-2/8/11 用 256,其他 Vol-2 用 255`
- **事实**: Vol-2-14 代码用 `img_volume1part2_group_256`(应该用 255)。KDoc L32 自我扩展了规则为 `Vol-2-2/8/11/14 用 256`,但这与 Vol-2-11/12 文档化版本冲突
- **影响**: 
  - 视觉一致性破坏 — 现在 Vol-2-13 (255) → Vol-2-14 (256) → Vol-2-15 (255) 看起来"两连 256"不是真的交替
  - 后续维护者按 Vol-2-11/12 权威文档选 255 会和 Vol-2-14 不一致
- **需用户确认**: Vol-2-14 用 256 是设计意图(交替扩展到 14)还是 bug?两种修法:
  - **设计**:同步 Vol-2-11/12/13 KDoc 为 `Vol-2-2/8/11/14 用 256`
  - **bug**:改 Vol-2-14 用 `img_volume1_group_255`,并把 KDoc L32 改回 `Vol-2-2/8/11 用 256`

### H8. Vol-2-8 L119 cross-screen drawable 引用脆弱 🔴
- **文件**: [Volume2Part8Screen.kt:119](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part8/Volume2Part8Screen.kt#L119)
- **问题**: `painterResource(R.drawable.img_volume2part7_image_289)` — 用的是 Vol-2-7 命名空间的 drawable
- **现状**: Vol-2-8 KDoc L41 注释解释了这是"复用 Vol-2-7 腾出的图"
- **影响**: 如果有人误删 `img_volume2part7_image_289`(以为 Vol-2-7 已经不用了),Vol-2-8 图2 会变 missing drawable
- **建议**: 复制资源为 `img_volume2part8_image_289.png`,或者把这个复用显式记入共享 helper

### H9. Vol-2-15 图2 突破系列布局网格
- **文件**: [Volume2Part15Screen.kt:116](android/app/srcrc/main/java/com/jueqiao/jianghu/ui/screens/volume2part15/Volume2Part15Screen.kt#L116)
- **问题**: `size(width = 365.dp, height = 324.dp)` — 系列其他屏图 1/2 都是 W=352 或 356,H=313 或 314
- **影响**: 图2 底边 = 481 + 324 = **805dp**,距离书框底 (Y=872) 只有 67dp — 系列最窄间距,可能在某些设备(16:9 + 导航条变大)图2 视觉上撞书框
- **建议**: 与 Vol-2-13/14 图2 对齐到 W=352 或 356,H=313 或 314

---

## 🟡 MEDIUM 摘要(6 项)

| # | 文件 | 问题 | 影响 |
|---|---|---|---|
| M1 | Vol-2-9/10/11/12/13 KDoc L32 | 交替模式列表 5 个不同版本("Vol-2-2/8" / "Vol-2-2/8/11" / "Vol-2-2/8/11/14"),无人同步 | grep 规则返回多个版本,未来加屏时猜错 |
| M2 | Vol-2-13/14/15 全部 | `\ No newline at end of file` × 3 文件 | ktlint/spotless CI 可能 fail,patch 工具异常 |
| M3 | Vol-2-15 L46 callback 名 `onOpenGunlun10` | 与系列 `onOpenVolume2PartN` 不一致 | 行为正确(跳到 Gunlun10 闭环),但 KDoc 未说明"为什么用 gunlun 而不是 vol2part16" |
| M4 | Vol-2-13 KDoc L32 | "与 Vol-2-9/10/12 同款"漏列 Vol-2-15(也用 255) | grep "255 screens" 漏一个,refactor 时漏改 |
| M5 | Vol-1-14 KDoc L33 | "Vol-13" 歧义(Vol-1-13 还是 Vol-2-13?) | 跨卷阅读混淆 |
| M6 | Vol-2-7/8/9 全套 | pre-PR 注释在多次 PR 中只动了 Modifier,没同步 KDoc — 8e5cded doc commit 也只动了 Vol-2-10~15,**漏修了 Vol-2-7/8/9** | 见 H2-H4 |

---

## ⚪ LOW 摘要(9 项)

| # | 文件 | 问题 | 建议 |
|---|---|---|---|
| L1 | Vol-2-13/14/15 modifier chain | 每次 recompose 重新分配 Modifier 实例 | `remember` 或提升为 top-level val |
| L2 | 9 屏 magic number | 书框 854×784 @ y=88,标题 213×32 @ 110,67,图位 X=18 Y=135/155 散落 9 文件 | 抽 `BookScreenDefaults` 常量 |
| L3 | 9 屏 Box → Image 嵌套冗余 | 每个 Image 包了一个仅做 align/offset/size 的 Box | 直接 Image 加 modifier 即可 |
| L4 | painterResource per recompose | bg + 书框 + 2 图 × 3 屏 = 12 次 painter 分配 | 配合 H6 抽 scaffold 时一并处理 |
| L5 | 标题 a11y | `Text` + `clickable` 缺 `Modifier.semantics { role = Role.Button }` | TalkBack 读为"普通文本"而非按钮 |
| L6 | Vol-2-14/15 标题 hit region | `.size(213.dp, 32.dp).clickable` 在某些 DPI/字体缩放下,可见 glyph 比 hit region 宽 | 加 `widthIn(min = ...)` 或 `wrapContentSize` |
| L7 | 新 callback `onOpenVolume2Part13/14/15` 命名 | 5 个 Vol-2 屏各不相同(11/12/13/14/15)| 配合 H6 scaffold 抽 `nextRoute: Route` |
| L8 | 缺 UI 测试 | 5 个新 callback 没有任何 click-to-navigation 测试覆盖 | 写 Compose UI test 验 Routes 路由 + callback 触发 |
| L9 | Vol-2-8 L41 KDoc | KDoc 解释了 image_289 跨屏复用,但缺 WARN 提示"不要删除此资源" | 加 ⚠ 标记 |

---

## Top 8 优先行动(按 ROI)

1. **🔴 修 H5 重复 `composable(Routes.Learning3)`** — 一行删除,但 NavGraph 构建会因此干净,后续路由 bug 减少
2. **🔴 修 H1 Vol-2-7 错图号** — 注释改 `image 289.png` → `image 292.png`,5 秒改完
3. **🔴 修 H2-H4 Vol-2-7/8/9 stale KDoc + 行内注释** — 按本次 8e5cded 同样方式批量对齐实际代码,约 9 处 Edit
4. **🔴 决定 H7 Vol-2-14 book-frame**:要 `256`(同步 11/12/13 KDoc)还是 `255`(改 Vol-2-14 + 15 KDoc)— 用户 30 秒判断
5. **🟡 修 M1 交替模式列表** — 6 个 KDoc 统一为同一字符串,grep 规则从此可靠
6. **🟡 修 M2 missing newline × 3** — IDE 自动加一行,顺手做
7. **🟢 抽 H6 `BookFrameScaffold`** — 大重构(预计 4-6 小时),但消除 H1-H4 + L1-L7 的根源
8. **🟢 写 L8 UI 测试** — 防 future dead button,5 个 callback 都有测试覆盖(预计 1-2 小时)

---

## 验证缺口

| 项 | 状态 |
|---|---|
| `compileDebugKotlin` | ✅ PASS(BUILD SUCCESSFUL 4s,纯注释改动 UP-TO-DATE)|
| `testDebugUnitTest` | ✅ PASS |
| 真机视觉验证(Vol-2-7/8/9 图位)| ❌ 未做 — H4 Vol-2-9 image_293 实际渲染与 KDoc 尺寸差异需肉眼确认是否裁剪 |
| 跨屏跳转链路闭环测试 | ❌ 未做 — Vol-1-14 → Gunlun6 → ... → Vol-1-14 与 Vol-2-15 → Gunlun10 → ... → Vol-2-15 的端到端没在设备上跑过 |
| H5 NavHost duplicate 实际行为 | ❌ 未跑 — Compose Navigation 对重复 route 的具体行为(警告 / 错 / shadow)需实测 |

---

## 与历次审计对比

| 维度 | v1 (09-09) | v2 (09-10) | v3 (本次 09-11) |
|---|---|---|---|
| Finding 总数 | 103 | 68 | **24**(本批次范围小)|
| NEW 缺陷 | - | - | **0 runtime bug**;注释/KDoc drift 是暴露 pre-existing |
| PERSISTED 缺陷 | - | - | N/A(范围不重叠)|
| HIGH 优先 | 8 | - | **9**(本文)|
| scaffold 抽离 | - | 已识别 | **仍无抽取**(本次 audit 已记录到 feedback)|

---

## 备注

- 本次审计范围是 `b04d25f` 合并引入的 16 Kotlin + 6 PNG + 1 md,**未覆盖** v1/v2 audit 中已识别的 9 个 maintainability HIGH(`BookShelfScaffold` / `LearningPageChrome` / `BookFrameScaffold` 等)— 见 Git 历史中的会话记录（`git show b5d2c8c:docs/SESSION-LOG-2026-09-11.md`）
- "Vol-2 doc 批本身 0 runtime bug" 是结论,但本次审计同时暴露了**已经在 main 上、之前未被识别的 pre-existing 缺陷**(Vol-2-7/8/9 stale、duplicate Learning3)— 这些不是本批次引入的,但既然在 audit 范围内,应当列出
