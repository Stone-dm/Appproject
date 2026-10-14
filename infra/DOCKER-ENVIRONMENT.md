# Docker 后端环境配置

> **适用读者**:新接手后端 / 真机调试的开发者(及 AI agent)。
> 给你一份"复制即可用"的 docker 后端环境说明 —— 涵盖 6 个服务、端口、启动、镜像加速、维护、踩坑。
> 配套文件:`infra/docker-compose.yml` + `infra/docker-compose.override.yml` + `infra/start-dev.ps1`

---

## 1. 架构概览

项目后端由 **6 个 Docker 容器**组成,统一在 `jianghu-dev_default` 这个 compose 网络里:

```
┌──────────── Android 端(USB 真机,见 ENV §3.1) ────────────┐
│  adb reverse tcp:8010 tcp:8010  →  本机 127.0.0.1:8010   │
└──────────────────────────┬─────────────────────────────────┘
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  api   (FastAPI,自建镜像 jianghu-dev-api:latest)              │
│         端口 8010 → 8000,Swagger UI /docs                     │
│         启动顺序:alembic upgrade head → seed → uvicorn       │
│  worker (后端 worker,自建镜像 jianghu-dev-worker:latest)        │
│         异步任务处理(生图 webhook / 媒体处理)                  │
└──────────┬──────────┬──────────────┬────────────┬─────────────┘
           ▼          ▼              ▼            ▼
       postgres    redis           minio        clamav
       (16-alpine) (7-alpine)   (对象存储)   (病毒扫描)
       55432→5432  16379→6379   19000/19001  内部 3310/7357
```

## 2. 服务清单

| 服务 | 镜像 | 容器名 | 宿主机端口 → 容器端口 | 作用 | 健康检查 |
|---|---|---|---|---|---|
| `postgres` | `postgres:16-alpine` | `jianghu-dev-postgres-1` | **55432 → 5432** | 主数据库 | `pg_isready -U jianghu` |
| `redis` | `redis:7-alpine` | `jianghu-dev-redis-1` | **16379 → 6379** | 缓存 + 任务队列 | `redis-cli ping` |
| `minio` | `minio:RELEASE.2025-07-23T15-54-02Z` | `jianghu-dev-minio-1` | **19000 → 9000**(S3 API)<br>**19001 → 9001**(Web console) | S3 兼容对象存储 | `curl http://localhost:9000/minio/health/live` |
| `clamav` | `clamav:1.4_base` | `jianghu-dev-clamav-1` | 内部 3310 / 7357 | 上传文件病毒扫描 | 无显式(由其它服务 `service_started` 触发) |
| `api` | `jianghu-dev-api:latest`(自建) | `jianghu-dev-api-1` | **8010 → 8000** | FastAPI 后端 + Swagger UI | `urllib.urlopen('http://127.0.0.1:8000/healthz')` |
| `worker` | `jianghu-dev-worker:latest`(自建) | `jianghu-dev-worker-1` | 内部 8000(不暴露) | 异步任务 worker | 由 `api` 触发 depends_on |

## 3. 启动步骤

### 3.1 前置条件

| 工具 | 版本 | 检查命令 |
|---|---|---|
| Docker Desktop | 4.x+ | `docker --version` |
| Docker Compose v2 | 随 Docker Desktop | `docker compose version` |
| Windows:WSL2 | 默认开启 | `wsl --status` |
| 磁盘空间 | 至少 10 GB 空闲 | `docker system df` |

### 3.2 启动(标准)

```powershell
cd infra
.\start-dev.ps1
```

脚本会:
1. `docker compose -f docker-compose.yml -f docker-compose.override.yml up -d`
2. 拉取/构建镜像(首次 ~3-5 分钟)
3. 启动 6 个容器,等 healthcheck 通过
4. **首次启动会跑 `alembic upgrade head` + seed 脚本**(这是 api 容器启动命令的一部分)

输出:
```
✓ 所有服务已启动(首次启动会下载镜像并运行 migrations,约 1-3 分钟)

API 地址:http://127.0.0.1:8010
Postgres: localhost:55432
Redis:    localhost:16379
MinIO:    localhost:19000(S3)/19001(console)
```

### 3.3 验证

```bash
# API 健康
curl http://127.0.0.1:8010/healthz
# 应回:{"status":"ok"}

# Swagger UI(浏览器打开)
http://127.0.0.1:8010/docs

# Postgres
psql -h 127.0.0.1 -p 55432 -U jianghu -d jianghu
# 密码见 .env(默认 local-development-only)

# Redis
redis-cli -h 127.0.0.1 -p 16379 ping
# 应回: PONG

# MinIO 控制台(浏览器)
http://127.0.0.1:19001
# 账号:jianghu-local / 密码:jianghu-local-secret-change-me
```

## 4. 镜像加速(国内网络)

`infra/docker-compose.override.yml` 切到 DaoCloud 镜像加速器,适用于 Docker Hub 拉取慢/失败的情况。

如果你用**阿里云 / 网易 / 其他**加速器,编辑 `infra/docker-compose.override.yml`:

```yaml
services:
  api:
    build:
      args:
        BASE_REGISTRY: registry.cn-hangzhou.aliyuncs.com/    # 改这里
  worker:
    build:
      args:
        BASE_REGISTRY: registry.cn-hangzhou.aliyuncs.com/    # 改这里
```

`Dockerfile` 用 `ARG BASE_REGISTRY` 接收,Dockerfile 本身不动。

## 5. 数据持久化

4 个 named volumes(`docker volume ls` 可看):

| Volume | 内容 | 重启保留? | 清理方式 |
|---|---|---|---|
| `jianghu-postgres` | Postgres 数据 | ✅ | `docker volume rm jianghu-postgres`(会**丢失数据库**) |
| `jianghu-redis` | Redis AOF 持久化 | ✅ | 同上 |
| `jianghu-minio` | MinIO 对象(bucket + 文件) | ✅ | 同上 |
| `jianghu-clamav` | ClamAV 病毒库 | ✅ | 重建会重新下载病毒库(~5 分钟) |

**想完全重置后端**(慎用):`docker compose down -v`

## 6. 维护操作

| 操作 | 命令 |
|---|---|
| 查看所有容器 | `docker ps` |
| 查看某服务日志 | `docker logs -f jianghu-dev-api-1` |
| 重启单个服务 | `docker compose -f infra/docker-compose.yml -f infra/docker-compose.override.yml restart api` |
| 全部停止 | `cd infra && .\stop-dev.ps1` |
| 全部停止 + 删除容器(保留 volumes) | `docker compose -f infra/docker-compose.yml -f infra/docker-compose.override.yml down` |
| 全部停止 + 删除容器 + 删除 volumes | `docker compose ... down -v` |
| 重新构建 api 镜像(代码改了 Dockerfile) | `docker compose ... build --no-cache api` |
| 进入 api 容器 shell | `docker exec -it jianghu-dev-api-1 sh` |
| 跑 alembic 迁移(手动) | `docker exec jianghu-dev-api-1 alembic upgrade head` |
| 跑 seed 脚本(手动) | `docker exec jianghu-dev-api-1 python -m app.domains.catalog.seed` |

## 7. 端口冲突排查

启动报 `bind: address already in use` → 端口被占用。

| 端口 | 谁在用? | 解决 |
|---|---|---|
| 8010 | 上次的 api 容器没退 | `docker ps` 找孤儿容器,`docker stop` + `docker rm` |
| 55432 | 本机有 PostgreSQL 16+ | 改 `infra/docker-compose.yml` 里 `ports: "55432:5432"` → `"55433:5432"`,然后同步改 `services/api/.env` 里 `JIANGHU_DATABASE_URL` |
| 16379 | 本机有 Redis | 同上,改端口 |
| 19000/19001 | 本机有 MinIO | 同上 |

**别忘了**改端口后 `adb reverse` 不受影响(`adb reverse tcp:8010 tcp:8010` 仍指向宿主机 8010),但 Android `local.properties` 的 `AUTH_BASE_URL` 要跟新端口同步。

## 8. 真机调试衔接(Android 端)

启动后端 + 跑通后:

```powershell
# 1. USB 真机连电脑,确认 adb 看到
adb devices
# 应回: 21908b7a    device

# 2. 设端口转发(每次插 USB/重启手机都要重设)
powershell -NoProfile -ExecutionPolicy Bypass -File infra\setup-reverse.ps1 -Device 21908b7a

# 3. Android 端 local.properties 配:
#    AUTH_BASE_URL=http://127.0.0.1:8010/
#    (Android Studio 里的 Gradle 会自动读这个)
```

详细见 `DEV-SETUP.md` 真机调试部分。

## 9. ⚠ 容易踩的坑

### 9.1 Docker Desktop 没启动

症状:`docker ps` 报 `Cannot connect to the Docker daemon`。
解决:Windows 系统托盘 → 右键 Docker 图标 → Start。**别用 `wsl` 直接跑 docker 命令**,Docker Desktop 才是后台。

### 9.2 WSL2 内存/CPU 限制

默认 WSL2 拿全部内存,可能让 Windows 卡顿。
解决:Docker Desktop → Settings → Resources → 限制 WSL 内存 4-6 GB / CPU 4 核。

### 9.3 Postgres 端口 55432 ≠ 标准 5432

**别用 5432** 访问,会连到本机 Postgres(如果有的话)。开发约定统一用 **55432**。

### 9.4 MinIO 公共访问端点

Android 端从手机访问图片要走 `MINIO_PUBLIC_ENDPOINT`,默认 `10.0.2.2:19000`(Android 模拟器 → 宿主机的特殊 IP)。**真机调试要改成 `127.0.0.1:19000` 并配合 `adb reverse tcp:19000 tcp:19000`**。

### 9.5 容器内 `localhost` ≠ 宿主机 `localhost`

容器里 `localhost` 指的是容器自己。访问其它容器要写服务名(`postgres` / `redis` / `minio` / `clamav`)。访问宿主机要走 `host.docker.internal`(Mac/Win)或宿主机内网 IP。

### 9.6 `alembic upgrade head` 失败

容器启动会跑 migration,如果失败 api 会反复重启。
解决:`docker logs jianghu-dev-api-1` 看错误,通常是因为之前 migration 文件被改了导致 schema 不一致。**别乱删 volume**,先看日志。

### 9.7 端口转发丢失

拔 USB / 重启手机 / 重启 adb server 都会丢 `adb reverse`。**不是 docker 问题,是 adb 问题**。重设:
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File infra\setup-reverse.ps1 -Device 21908b7a
```

### 9.8 镜像拉取慢/超时

切到 DaoCloud override(见 §4)。如果还慢,试试 `docker pull postgres:16-alpine` 单个拉,看哪步卡。

## 10. 服务升级 / 改镜像版本

修改 `infra/docker-compose.yml` 的 `image: 镜像:tag`,然后:
```bash
docker compose -f infra/docker-compose.yml -f infra/docker-compose.override.yml pull
docker compose -f infra/docker-compose.yml -f infra/docker-compose.override.yml up -d
```

`api` / `worker` 是自建镜像,改代码后:
```bash
docker compose ... build --no-cache api
docker compose ... up -d
```

## 11. 关联文档

- `ENVIRONMENT.md`:总览技术栈 / Android 构建 / 项目结构
- `DEV-SETUP.md`:真机调试步骤 + adb reverse 设置
- `services/api/README.md`:后端 API 自身说明(如有)
- `infra/`:所有 docker / 启动脚本都在这里

---

**最后更新**:2026-10-13(基于 `docker ps` 实际状态 + `infra/docker-compose.yml` 内容)
