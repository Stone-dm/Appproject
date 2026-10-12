# 机巧江湖 · 主工程

跨机器交接入口：[Git 仓库使用说明](handover/README.md)、[完整项目交接文档](docs/handover/项目交接文档.md)、[AI 项目地图](handover/project.json)。仓库内初始化工具可重新生成本机配置，原机器密钥不随源码交付。完整离线源码、EXE/APK、依赖安装包及设计原件在另行交付的完整 ZIP 中。

这是机巧江湖当前的开发主线：原生 Android 客户端与 Python / FastAPI 服务端。旧 Expo / React Native 的 `mobile/` 工程已不在本仓库，不能继续使用它的网页截图脚本或开发步骤。

## 工程构成

- `android/`：Kotlin + Jetpack Compose 客户端，Gradle 单应用模块 `:app`。业务包括账户、首页、修炼、创作、作品档案、大会、行囊和设置。
- `services/api/`：FastAPI 后端，含 `app/` 业务模块、`migrations/` 数据库迁移、`tests/` 自动测试、`scripts/` 验证与验收工具、`seed_assets/` 演示媒体。
- `packages/learning-contracts/`：TypeScript 学习接口契约及请求客户端，是接口交付材料；并非 Android 构建依赖。
- `infra/`：Docker Compose、本地服务启动/停止、ADB 转发、Android 安装和端到端验收脚本。
- `scripts/`：当前资源生成工具。`generate_mechanical_butterfly_video.py` 生成后端演示视频与封面。
- `docs/`：当前开发说明、业务实现约定、验收指南及少量仍有引用的历史审计。

`android/app/src/main/res/`、后端迁移与种子内容、两端正式测试均属于工程维护所需内容。`build/`、`.gradle/`、`__pycache__/`、`artifacts/`、`output/` 和 `tmp/` 是可重建或临时输出，不是源码。

## 配置与启动

- 环境要求与命令：[ENVIRONMENT.md](ENVIRONMENT.md)
- Android 构建与配置：[android/README.md](android/README.md)
- 后端启动、迁移、种子与测试：[services/api/README.md](services/api/README.md)
- 协作规则：[CONTRIBUTING.md](CONTRIBUTING.md)
- 文档索引：[docs/README.md](docs/README.md)

本机配置保存在 `android/local.properties` 和 `services/api/.env`，不要提交。仓库外的 `../work/submission-local/` 是离线演示交付版本，含主工程没有的适配代码，不能当成重复源码整目录删除。

## 验证命令

在仓库根目录执行隔离测试入口，避免本机 `.env` 在模块导入时连接开发服务：

```powershell
py -3.13 handover/manage.py test
```

从本目录进入 `android/`，先为当前终端选择可用的 JDK/JBR 21：

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

依赖已经缓存时可加 `--offline`。正式测试不是临时测试文件，不要在整理目录时删除。

## 资料与历史输出

项目工作区的设计和策划资料在仓库外的 `../文档/`、`../设计稿/`、`../人设图/` 和 `../icon/`。失效的旧预览脚本已删除，`../预览/` 目录已移除。旧演示成片、录屏、截图、预览视频及其压缩副本已于 2026-10-11 按用户要求永久删除，原 `../交付/演示视频/` 不再存在。应用内种子媒体、设计原图、正式构建交付包和数据库备份仍保留。

[2026-10-11 清理报告](docs/PROJECT-CLEANUP-2026-10-11.md)记录删除依据、保留内容、归档位置和验证结果。历史审计报告描述当时状态，不能替代当前源码与本说明。
