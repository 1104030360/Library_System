# Multi-stage build for Frontend
# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# 複製 package files
COPY package*.json ./

# 安裝依賴
RUN npm ci

# 複製源碼
COPY . .

# 建置生產版本
RUN npm run build

# Stage 2: Production
FROM nginx:alpine

# 複製建置結果到 nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# 複製 nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80 443

# 健康檢查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

# 啟動 nginx
CMD ["nginx", "-g", "daemon off;"]
