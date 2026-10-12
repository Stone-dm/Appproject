# 创作流程验收指南

2026-10-11 整理。本文保留当前工具的复跑入口，替代旧演示验收报告。历史账号、截图、APK 哈希和通过数字不再作为当前结果；旧演示证据已按要求删除。

## 前置条件

- 已按 [环境说明](../ENVIRONMENT.md) 准备 JDK/JBR 21、Android SDK、API 虚拟环境和构建依赖。
- `adb devices` 能识别待验收设备，命令中的设备编号替换为实际值。
- `demo` / `acceptance` 包连接 `http://10.0.2.2:8011/`；下方命令适用于能够通过该地址访问本机服务的 Android 模拟器。
- 使用专用验收设备和数据：脚本会安装对应构建包，并清空该包的应用数据。

## 运行完整验收

在主工程根目录执行其中一个命令：

```powershell
.\infra\run-demo-acceptance.ps1 -Mode demo -Device emulator-5554
.\infra\run-demo-acceptance.ps1 -Mode acceptance -Device emulator-5554
```

入口脚本检查后端健康状态，必要时启动 `services/api/scripts/serve_acceptance.py`，构建、安装应用并调用 `android_creation_acceptance.py`。新结果写入 `artifacts/creation-acceptance/`，其中包含本次构建信息和设备流程证据。

已有后端时可使用 `-SkipApiStart`；服务不可用时该选项会让流程中止。完整验收不要使用 `-SkipWorkflowCheck` 或 `-SkipDeviceFlow`，否则不能据此认定设备流程通过。

## 检查与记录

- 覆盖登录、创意与工法、草图、制作、测试、封卷、投递、审核和档案继续创作，并按脚本检查异常及状态恢复路径。
- 同时记录提交版本、本次 APK 哈希、设备、实际命令和结果。部分步骤失败时保留失败原因，不沿用上次的通过结论。
- 后端正式测试与 Android 单元测试/构建命令见 [主工程说明](../README.md)；设备验收不能替代这些检查。

验收服务采用确定性开发生成器，不代表线上生成模型的画质或耗时。离线打包版的运行及重启验证是另一条流程，参见 [离线工程说明](../../work/submission-local/README.md)。
