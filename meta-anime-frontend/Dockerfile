# 多阶段构建 - 构建阶段
FROM node:25.2.1 AS build

# 安装并启用 corepack
RUN npm install -g corepack --force && corepack enable

WORKDIR /app

# 复制 package 文件
COPY package.json pnpm-lock.yaml ./

# 准备 pnpm
RUN corepack prepare --activate

# 安装依赖
RUN pnpm install --frozen-lockfile

# 复制源代码
COPY . .

# 构建生产版本
RUN pnpm run build

# 生产阶段 - 使用 nginx 提供静态文件
FROM nginx:1.29.4

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=build /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
