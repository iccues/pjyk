# 构建阶段
FROM node:25.2.1 AS build

RUN npm install -g corepack --force && corepack enable

WORKDIR /app

# 激活 pnpm
COPY package.json ./
RUN corepack prepare --activate

# 安装依赖
COPY pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/client/package.json ./apps/client/
COPY apps/admin/package.json ./apps/admin/
COPY packages/shared/package.json ./packages/shared/
RUN pnpm install --frozen-lockfile

# 构建
COPY . .
RUN pnpm turbo run build

# 生产阶段
FROM nginx:1.29.4

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/apps/client/dist /usr/share/nginx/client
COPY --from=build /app/apps/admin/dist /usr/share/nginx/admin

EXPOSE 80 81

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
