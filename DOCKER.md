# Docker Deployment Guide

本文件說明如何使用 Docker 部署圖書館管理系統。

---

## 前置需求

### 安裝 Docker

**macOS**:
```bash
# 下載 Docker Desktop
# https://www.docker.com/products/docker-desktop/

# 或使用 Homebrew
brew install --cask docker
```

**Linux**:
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install docker-compose
sudo apt-get install docker-compose
```

**Windows**:
- 下載 Docker Desktop: https://www.docker.com/products/docker-desktop/

### 驗證安裝

```bash
docker --version
docker-compose --version
```

---

## 快速開始

### 方法 1: 使用啟動腳本（推薦）

```bash
./docker-run.sh
```

這個腳本會自動：
1. 檢查 Docker 是否安裝且執行中
2. 建立 Docker image
3. 啟動容器
4. 顯示訪問資訊

### 方法 2: 使用 docker-compose

```bash
# 建立並啟動
docker-compose up -d

# 查看日誌
docker-compose logs -f

# 停止
docker-compose down
```

### 方法 3: 使用 Docker 指令

```bash
# 建立 image
docker build -t library-api:latest .

# 執行容器
docker run -d \
  --name library-api-server \
  -p 7070:7070 \
  -v "$(pwd)/data:/app/data" \
  library-api:latest

# 查看日誌
docker logs -f library-api-server

# 停止並移除容器
docker stop library-api-server
docker rm library-api-server
```

---

## 訪問應用

容器啟動後，訪問：

- **首頁**: http://localhost:7070
- **登入頁**: http://localhost:7070/login.html
- **API 狀態**: http://localhost:7070/api/status

---

## Docker 架構說明

### Dockerfile

使用 **Multi-stage build** 策略：

```dockerfile
# Stage 1: Build
FROM openjdk:17-slim AS builder
# ... compile Java files

# Stage 2: Runtime
FROM openjdk:17-slim
# ... copy compiled files, run server
```

**優點**：
- 最終 image 更小（不包含編譯工具）
- 更快的部署速度
- 更好的安全性

### docker-compose.yml

```yaml
services:
  library-api:
    build: .
    ports:
      - "7070:7070"
    volumes:
      - ./data:/app/data    # SQLite 資料持久化
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:7070/api/status"]
```

**功能**：
- Port mapping: 7070 (host) → 7070 (container)
- Volume mount: 資料庫檔案持久化
- Health check: 自動檢查服務健康狀態
- Auto restart: 容器異常時自動重啟

### .dockerignore

排除不必要的檔案，減小 build context：
- `.git/`, `.idea/`, `.vscode/`
- `legacy/`, `test/`
- `*.md` (除了 README)
- `data/*.db` (資料庫會重新建立)

---

## 常用指令

### 容器管理

```bash
# 查看執行中的容器
docker-compose ps

# 停止容器
docker-compose stop

# 啟動容器
docker-compose start

# 重啟容器
docker-compose restart

# 停止並移除容器
docker-compose down

# 停止並移除容器 + volumes
docker-compose down -v
```

### 日誌查看

```bash
# 查看所有日誌
docker-compose logs

# 即時追蹤日誌
docker-compose logs -f

# 查看最後 100 行
docker-compose logs --tail=100

# 只看特定時間的日誌
docker-compose logs --since 10m
```

### Image 管理

```bash
# 列出 images
docker images

# 重新建立 image（強制）
docker-compose build --no-cache

# 移除舊的 image
docker image prune

# 移除所有未使用的 images
docker image prune -a
```

### 進入容器

```bash
# 進入容器 shell
docker-compose exec library-api bash

# 或使用 docker 指令
docker exec -it library-api-server bash

# 在容器內執行指令
docker-compose exec library-api ls -la /app/data
```

### Health Check

```bash
# 查看健康狀態
docker-compose ps

# 手動執行 health check
curl http://localhost:7070/api/status
```

---

## 資料持久化

### SQLite 資料庫

資料庫檔案儲存在 `./data/library.db`，透過 volume mount 持久化：

```yaml
volumes:
  - ./data:/app/data
```

**優點**：
- 容器重啟/重建時，資料不會遺失
- 可以直接在 host 上備份資料庫
- 方便資料庫遷移

### 備份資料庫

```bash
# 備份
cp data/library.db data/library.db.backup

# 或打包
tar -czf library-backup-$(date +%Y%m%d).tar.gz data/

# 還原
cp data/library.db.backup data/library.db
docker-compose restart
```

---

## 故障排除

### 問題 1: Port 7070 already in use

**原因**：Port 已被其他程式佔用

**解決方法**：
```bash
# 找出佔用 port 的程序
lsof -i :7070

# 停止該程序，或修改 docker-compose.yml 使用其他 port
ports:
  - "8080:7070"  # host:container
```

### 問題 2: Container keeps restarting

**原因**：應用啟動失敗

**解決方法**：
```bash
# 查看日誌
docker-compose logs library-api

# 檢查編譯錯誤
docker-compose build --no-cache
```

### 問題 3: Database permission denied

**原因**：data/ 目錄權限問題

**解決方法**：
```bash
# 修改權限
chmod 755 data/
chmod 644 data/library.db

# 或重建容器
docker-compose down
sudo rm -rf data/
docker-compose up -d
```

### 問題 4: Cannot connect to Docker daemon

**原因**：Docker Desktop 未啟動

**解決方法**：
1. 啟動 Docker Desktop
2. 等待 Docker 完全啟動（狀態列圖示變綠）
3. 重新執行 `./docker-run.sh`

---

## 效能調整

### 記憶體限制

```yaml
services:
  library-api:
    mem_limit: 512m
    environment:
      - JAVA_OPTS=-Xmx256m -Xms128m
```

### CPU 限制

```yaml
services:
  library-api:
    cpus: 0.5  # 限制使用 0.5 個 CPU core
```

---

## 生產環境建議

### 1. 使用環境變數

```yaml
environment:
  - PORT=${PORT:-7070}
  - DB_PATH=${DB_PATH:-/app/data/library.db}
```

### 2. 使用外部資料庫

對於生產環境，考慮使用獨立的資料庫服務：

```yaml
services:
  library-api:
    depends_on:
      - postgres
    environment:
      - DB_TYPE=postgres
      - DB_HOST=postgres
      - DB_PORT=5432

  postgres:
    image: postgres:15
    volumes:
      - postgres-data:/var/lib/postgresql/data
```

### 3. 使用 HTTPS

```yaml
services:
  nginx:
    image: nginx:alpine
    ports:
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
```

### 4. 監控與日誌

```yaml
services:
  library-api:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

---

## 部署到雲端

### Docker Hub

```bash
# 登入 Docker Hub
docker login

# 標記 image
docker tag library-api:latest username/library-api:latest

# 推送到 Docker Hub
docker push username/library-api:latest

# 在其他機器拉取並執行
docker pull username/library-api:latest
docker run -d -p 7070:7070 username/library-api:latest
```

### AWS ECS / Azure Container Instances

使用 docker-compose.yml 可以直接部署到雲端容器服務：

```bash
# AWS ECS
ecs-cli compose up

# Azure
az container create --resource-group mygroup --file docker-compose.yml
```

---

## 開發模式

### Hot Reload（開發中）

```yaml
services:
  library-api:
    volumes:
      - ./backend/src:/app/backend/src
      - ./web:/app/web
    command: sh -c "javac ... && java ..."
```

### 掛載測試

```yaml
services:
  library-api:
    volumes:
      - ./test:/app/test
```

---

## 清理與維護

### 清理未使用的資源

```bash
# 清理停止的容器
docker container prune

# 清理未使用的 images
docker image prune

# 清理未使用的 volumes
docker volume prune

# 清理所有（危險！）
docker system prune -a --volumes
```

### 查看磁碟使用

```bash
docker system df
```

---

## 總結

**優點**：
- ✅ 一致的執行環境
- ✅ 簡單的部署流程
- ✅ 資料持久化
- ✅ 易於擴展
- ✅ 跨平台支援

**使用場景**：
- 🎯 開發環境統一
- 🎯 測試環境部署
- 🎯 生產環境容器化
- 🎯 CI/CD 整合

**Linus 說**: "Containers are just fancy chroot with better isolation. Simple. Works."

---

## 相關連結

- [Dockerfile 參考](Dockerfile)
- [docker-compose.yml 參考](docker-compose.yml)
- [主要 README](README.md)
- [Docker 官方文件](https://docs.docker.com/)
