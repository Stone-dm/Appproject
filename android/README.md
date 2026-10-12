# 机巧江湖 Android 客户端

原生 Kotlin / Jetpack Compose 客户端；主工程为 Gradle 单模块 `:app`。旧 React Native / Expo `mobile/` 目录已不在仓库。

## 代码与资源

- `app/src/main/java/com/jueqiao/jianghu/`：应用入口、账户、创作、行囊、页面路由及 UI。
- `app/src/main/res/`：界面素材、字体、图标、字符串和网络安全配置。
- `app/src/test/`：模型、路由、手势和布局等单元测试，属于持续维护内容。
- `gradle/libs.versions.toml`：依赖版本的唯一声明入口。
- `gradle/wrapper/`、`gradlew`、`gradlew.bat`：已随仓库提供，包含 `gradle-wrapper.jar`，无需重新生成。
- `branding/`、`docs/`：品牌参考与屏幕适配说明。

当前应用从 `Routes.Splash` 启动，业务包括修炼、创作、作品档案、大会和行囊，已存在配套后端接口与状态管理。不能依据早期 README 将所有非认证模块视为纯静态演示。

## 构建与测试

本仓库采用 Gradle 8.10.2、AGP 8.7.3、Kotlin 2.0.21，Android SDK 35，最低支持 SDK 24。编译字节码目标为 Java 17；本机使用 Android Studio 自带 JBR 21 运行 Gradle。

从本目录执行：

```powershell
$env:JAVA_HOME = 'C:/Program Files/Android/Android Studio/jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

依赖已经缓存时可加 `--offline`。APK 在 `app/build/outputs/apk/debug/app-debug.apk`。`build/` 是输出目录，清理后会重新生成。

## 本机配置

使用 `local.properties.example` 作为参考，在 `local.properties` 设置实际 SDK 路径及 API 地址。此文件不提交：

```properties
AUTH_BASE_URL=http://10.0.2.2:8010/
TERMS_VERSION=2026-08
PRIVACY_VERSION=2026-08
```

协议版本应与实际后端部署一致；上述值是当前构建脚本默认值。USB 真机可将 API 地址设为 `http://127.0.0.1:8010/` 并运行 `adb reverse tcp:8010 tcp:8010`。Debug 有本机 HTTP 调试配置，Release 关闭明文网络。

构建变体包括 `debug`、`release`、`demo`、`acceptance`；后两者有独立包名后缀，连接 8011 的演示后端。具体启动方式见 `../infra/run-demo-acceptance.ps1`。

## 维护约定

图片资源只有在源代码/XML 引用检查、Lint 和构建验证共同支持时才可移除。不同密度目录的同名文件、接口返回的内容和正式测试不能只凭文件名判断重复。

项目外的设计源图在工作区资料目录保存，应用实际使用的版本必须保留在本模块资源目录中。不要把模拟器截图、安装包备份或下载的字体压缩包堆在本目录。

后端配置见 [API 说明](../services/api/README.md)，项目职责见 [主工程说明](../README.md)。
