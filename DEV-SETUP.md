# 本地开发快速入口

> 2026-10-11 更新。已移除旧机器专用路径、旧启动页及失效的环境假设；历史文本保存在本次工作区外归档中。

## 首次配置

1. 阅读 [环境说明](ENVIRONMENT.md)，准备 JDK/JBR 21、Android SDK 35 和 Python 3.13。
2. 参考 `android/local.properties.example` 配置自己的 `android/local.properties`。
3. 参考 `services/api/.env.example` 配置自己的 `.env`。已有配置不要覆盖。
4. 按 [API 说明](services/api/README.md) 安装虚拟环境依赖、启动本地服务、迁移并导入种子。

## 日常流程

在主工程根目录执行：

```powershell
docker compose -f infra/docker-compose.yml -f infra/docker-compose.override.yml up -d
```

USB 真机调试时确认 `adb devices` 输出，再执行：

```powershell
adb reverse tcp:8010 tcp:8010
```

使用 Android Studio 打开 `android/`，选择可用的 JBR 21；或从当前终端执行：

```powershell
$env:JAVA_HOME = 'C:/Program Files/Android/Android Studio/jbr'
cd android
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --console=plain
```

按实际设备地址配置 API：模拟器使用 `10.0.2.2`，ADB reverse 真机使用 `127.0.0.1`。不要照抄历史文档中的他人用户名、设备序列号或局域网地址。

## 问题定位

- 构建启动失败：检查当前终端 `JAVA_HOME` 与 Android Studio 的 Gradle JDK。
- 登录无法连接：检查后端 `http://127.0.0.1:8010/healthz`、客户端 API 地址及端口转发。
- 后端环境配置解析失败：列表型配置按 `.env.example` 使用 JSON 数组。
- 新资源或代码未生效：重新构建对应变体，不安装旧 `artifacts/` 中的历史 APK。

有效验证命令和清理边界见 [README](README.md)。会话日志与旧审计是历史材料，不作为当前操作步骤。
