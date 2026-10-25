# 构建阶段
FROM node:24.22.1 AS build

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
RUN pnpm install --offline --frozen-lockfile && pnpm --filter @pjyk-web/client build

# 生产阶段
FROM caddy:2.11.7

COPY Caddyfile /etc/caddy/Caddyfile
COPY --from=build /app/apps/client/dist /srv/client

EXPOSE 80 443 443/udp

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -q --spider http://127.0.0.1/health || exit 1
