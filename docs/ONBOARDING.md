# 新人入门

本指南于 2026-10-11 按当前工程整理。日常开发目录是 `Appproject/`，Android Studio 打开其中的 `android/`。

## 先读这些

1. [主工程说明](../README.md)：Android、FastAPI、接口契约与基础设施的职责。
2. [环境说明](../ENVIRONMENT.md) 和 [本地启动](../DEV-SETUP.md)：JDK/JBR、SDK、Python、本机配置及启动命令。
3. 按任务阅读 [Android 说明](../android/README.md) 或 [API 说明](../services/api/README.md)。
4. 修改页面前阅读 [协作约定](../CONTRIBUTING.md) 和 [屏幕适配](../android/docs/screen-adaptation.md)。业务说明见 [文档索引](README.md)。

## 开发时的约定

- UI、ViewModel、Repository/API 分工沿用现有实现；应用依赖由 `JianghuApp.kt` 手动组装。
- 新页面从相近页面开始，遵循既有命名、路由和资源约定；目录整理不包含统一抽象或依赖注入框架迁移。
- 修改页面位置、尺寸时同步相关注释；有意义的交互提供无障碍说明。
- API 地址、SDK 路径与后端密钥放在本机配置中；不要覆盖他人的配置或输出凭据。
- 模拟器和 USB 真机均可调试。按实际设备配置地址和 ADB 转发，不复制旧日志中的设备序列号或他人机器路径。

## 改完后怎么验证

后端在 `services/api/` 运行：

```powershell
.\.venv\Scripts\python.exe -m pytest -p no:cacheprovider
```

Android 在 `android/` 选择可用的 JDK/JBR 21 后运行：

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

交互流程还需在实际目标设备验证；完整创作流程见 [验收指南](creation-demo-acceptance.md)。测试数量和通过状态以本次运行输出为准，不沿用旧报告。

## 保持目录清晰

正式源码、资源、迁移、契约、有效测试及本机配置保留。生成结果使用各模块 `build/` 或主工程 `artifacts/`；临时操作使用 `tmp/`。不要因为文件有旧日期、版本号或 `test` 字样就删除。

工作区外层的 `work/submission-local/` 是独立离线交付工程，含专用入口和打包依赖；它不是日常修改主线，也不能按重复副本整目录删除。设计原稿与策划材料在工作区各资料目录。

不再要求阅读全部历史会话或为每次会话增加日志。修改已有文档时说明当前行为；需要回溯时查询 Git 历史。提交、推送和发布仍按当前任务的授权执行。
