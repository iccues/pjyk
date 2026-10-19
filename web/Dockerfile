# 构建阶段
FROM node:25.2.1 AS build

RUN npm install -g corepack --force && corepack enable

WORKDIR /app

# 激活 pnpm
COPY package.json ./
RUN corepack prepare --activate

# 只凭 lockfile 下载依赖，源码改动不会让这一层缓存失效
COPY pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm fetch

# 安装并构建
COPY . .
RUN pnpm install --offline --frozen-lockfile && pnpm -r build

# 生产阶段
FROM nginx:1.29.4

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/apps/client/dist /usr/share/nginx/client
COPY --from=build /app/apps/admin/dist /usr/share/nginx/admin

EXPOSE 80 81

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl --fail --silent --show-error http://localhost/health > /dev/null || exit 1

CMD ["nginx", "-g", "daemon off;"]
