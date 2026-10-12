# 项目环境说明

> 2026-10-11 按当前仓库配置校正。依赖版本以 Gradle 版本目录和 `services/api/pyproject.toml` 为准。

## 当前工程

主工程由 Android 客户端、FastAPI 后端、接口契约和开发基础设施组成。旧 `mobile/` Expo 工程已移除。Android 包名为 `com.jueqiao.jianghu`，版本为 `2.0`，应用从 `Routes.Splash` 启动。

Android 使用 Gradle 8.10.2、Android Gradle Plugin 8.7.3、Kotlin 2.0.21、Compose BOM 2024.12.01；编译字节码目标为 Java 17，`compileSdk/targetSdk` 为 35，`minSdk` 为 24。

后端使用 Python 3.12–3.14（本机虚拟环境使用 3.13）、FastAPI、SQLAlchemy/Alembic、PostgreSQL、Redis、MinIO 及媒体处理 Worker。完整依赖声明在 `services/api/pyproject.toml`。

## 本机配置

- Android SDK 路径写在 `android/local.properties` 的 `sdk.dir`。
- API 地址写在同文件的 `AUTH_BASE_URL`。模拟器常用 `http://10.0.2.2:8010/`；USB 真机使用 ADB 反向端口映射及 `http://127.0.0.1:8010/`。
- 后端密钥及服务配置保存在 `services/api/.env`，参考 `.env.example`。
- 上述本机文件已被忽略，不能提交或放入交付包。

Gradle 运行时使用 JDK/JBR 21。本机已验证的路径是 `C:/Program Files/Android/Android Studio/jbr`。下面的设置只影响当前 PowerShell，不修改全局环境变量：

```powershell
$env:JAVA_HOME = 'C:/Program Files/Android/Android Studio/jbr'
cd android
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

其他机器使用其实际 JDK/JBR 路径。当前 `gradle.properties` 没有硬编码 `org.gradle.java.home`；不要依据旧文档假设存在。`local.properties` 中任意自定义 `java.home` 也不能代替已明确配置的 Gradle JDK。

## 后端开发

在仓库根目录执行：

```powershell
docker compose -f infra/docker-compose.yml -f infra/docker-compose.override.yml up -d
```

或者进入 `infra/` 后运行 `.\start-dev.ps1`。API 默认映射到 8010；PostgreSQL、Redis、MinIO 的本机端口以 Compose 文件为准。数据库迁移和种子导入步骤见 [API 说明](services/api/README.md)。

在 `services/api/` 下执行测试：

```powershell
.\.venv\Scripts\python.exe -m pytest -p no:cacheprovider
```

已有测试使用隔离的内存 SQLite 等替代服务，不需要启动生产数据库。不得把本地 `.env` 或真实数据库作为清理目标。

## 构建版本

- `debug`：日常开发，读取本机 `AUTH_BASE_URL`。
- `release`：启用 R8 与资源压缩；正式服务使用 HTTPS。
- `demo` / `acceptance`：独立包名后缀，对接 8011 的演示/验收后端。

演示与验收流程见 `infra/run-demo-acceptance.ps1`，执行前阅读其参数与设备数据重置行为。工作区外层 `work/submission-local/` 另有离线参赛适配，不属于这里的构建变体。

## 日常维护边界

必须保留：`gradle/wrapper/`、源代码、Android 资源、本地配置、后端迁移、种子内容、接口 fixtures、正式测试与必要工具。

可重新生成：模块 `build/`、`.gradle/`、`.kotlin/`、Python 字节码缓存和旧覆盖率报告。临时截图与日志放 `artifacts/`、`output/` 或 `tmp/`，不提交；需要长留的设计原稿和成片应移入工作区的正式资料目录。

Android Manifest 已声明 INTERNET 权限，当前启动页为 Splash；旧文档中的“缺少网络权限”“启动 ChatResult”“只有认证接后端”等说法不再作为当前工程说明。

参见 [主工程结构](README.md)、[Android 构建](android/README.md) 和 [清理记录](docs/PROJECT-CLEANUP-2026-10-11.md)。
