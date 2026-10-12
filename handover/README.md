# Git 仓库交接与使用

更新日期：2026-10-12。此目录让接手人和 AI 在单独克隆 `Appproject` 后，能够找到项目说明、初始化工具和已验证的依赖版本。

先读 [主工程说明](../README.md)、[完整交接文档](../docs/handover/项目交接文档.md) 和 [协作约定](../CONTRIBUTING.md)。AI 同时读取 [项目地图](project.json)。完整文档也提供 [HTML 阅读版](../docs/handover/项目交接文档.html)。

## Git 仓库与完整 ZIP 的区别

本仓库根目录直接包含 `android/`、`services/api/`、`infra/`、`packages/` 和 `handover/`。完整 ZIP 的外层还有 `Appproject/`、`work/submission-local/`、设计资料和体验运行文件。

`docs/handover/` 保留的是 2026-10-11 完整 ZIP 交接说明快照，其路径、目录树、交付范围和命令按完整 ZIP 根目录解释。仅克隆 Git 时，以本页的命令为准，不要创建第二个 `Appproject/` 或误以为 Git 自带 EXE/APK。

单独交付的文件：

- `JiqiaoJianghu-Handover-20261011.zip`：完整主线/离线源码、设计原件、64 个 Python wheel、离线 EXE/APK 和逐文件校验清单。
- `JiqiaoJianghu-Restricted-Backup-20261011.zip`：历史数据库，默认不恢复，只交给有权限的接手人；该文件未加密。
- `机巧江湖-完整交接总包-20261012.zip`：上述文件与校验信息的集合，已放在原交付电脑桌面。Git 不包含这三个 ZIP，也不自动提供在线下载地址。

## 单独克隆后继续开发

复现基线是 Windows x64 / Python 3.13；Android 使用 Android Studio、JDK/JBR 21、SDK 35。下面的命令均在 Git 仓库根目录运行：

```powershell
py -3.13 handover/manage.py doctor
py -3.13 handover/manage.py setup --online
py -3.13 handover/manage.py test
py -3.13 handover/manage.py build-android --variant debug --java-home "C:/Program Files/Android/Android Studio/jbr"
```

`setup --online` 从配置的 Python 软件源获取 `requirements-win-py313.txt` 中的固定依赖，再 editable 安装当前 API。已有 `.env` 和 `local.properties` 不会被覆盖；缺失时生成独立随机配置。SDK 不在默认位置时执行 `configure --sdk "D:/Android/Sdk"`。未提供在线模型账户权限时，初始化默认禁用对话教练；生产发布仍需单独配置和验收。

有完整 ZIP 的 wheel 目录时，可以复制到本仓库 `handover/wheels-win-py313/`，或保持外层 `../handover/wheels-win-py313/`，执行不带 `--online` 的 setup。本仓库不把可重建虚拟环境、第三方 wheel 或构建缓存纳入 Git。首次 Android 构建需要网络获取依赖，只有已缓存时才加 `--offline`。

测试入口使用临时工作目录并清除继承的 `JIANGHU_*` 变量，避免真实本机配置在导入时干扰测试；各测试自行创建隔离环境。正式服务启动、迁移和 MinIO 地址说明见 [API README](../services/api/README.md) 与 [环境说明](../ENVIRONMENT.md)。

## 工具与离线版

- `manage.py`：检查依赖、初始化、后端测试、Android 构建；按本仓库根目录定位。
- `requirements-win-py313.txt`：2026-10-11 已验证环境的精确依赖快照；项目依赖范围仍以 `services/api/pyproject.toml` 为准。
- `build_offline.py`：需要完整 ZIP 中的独立离线源码，位置是仓库同级 `../work/submission-local/`。只有主线 clone 时会明确提示缺失，不会伪造离线工程。
- `render_docs.ps1`：用 PowerShell 7 从 `docs/handover/*.md` 再生成同目录 HTML。
- `project.json`：供 AI 读取的目录、命令和维护约定；路径以 Git 根为准。
- `verification.json`、`verification/`：2026-10-11 交接验证快照及精简结果，不代表后续任意提交都重新验收。

完整 ZIP 的 `verify-package` 必须在完整 ZIP 外层运行，Git clone 没有它的逐文件清单。离线业务独有入口不能靠主线源码代替；桌面 ZIP 是固定快照，不会随 Git 提交自动更新。

## 已有验证与限制

2026-10-11：后端 130 个测试通过；Android 主线和离线版分别 50 个单元测试通过；主线构建及 Lint 通过（0 errors，219 warnings，3 information）；离线 EXE/APK 从源码重建，EXE 与源码服务通过隔离数据的业务和重启检查。详细条件见 [交接验证记录](verification.json)。

2026-10-12：当前主线再次构建成功，并安装到 Pixel 6 / Android 14 原生模拟器，确认首页显示；现有开发数据库为 `0026_video_media`，启动 API、Worker 及依赖后健康检查成功。本次启动没有验证全部 UI、在线模型/短信、生产迁移、正式签名或历史备份恢复。

修改前检查实际代码、Git 状态与本机配置；不要输出密钥。保留正式测试、迁移、契约、应用资源及独有离线源码。新增运行日志和临时截图放到忽略目录，不随每次工作重复新增历史报告。
