# pjyk-web

`pjyk-web` 是前端 monorepo，使用 pnpm workspace 管理，包含：

- `apps/client`：公开站点，提供首页、搜索、列表、详情和文档页
- `apps/admin`：后台管理站点
- `packages/shared`：前后端共享的前端常量、平台配置和工具函数
- `packages/tsconfig`：统一的 TypeScript 配置基座

## 技术栈

- Vue 3
- TypeScript
- Vite
- Tailwind CSS
- urql GraphQL 客户端
- TanStack Query、Pinia、Axios 等后台依赖

## 目录

```text
web/
├── apps/
│   ├── client/
│   └── admin/
├── packages/
│   ├── shared/
│   └── tsconfig/
├── package.json
└── pnpm-workspace.yaml
```

## 工作区说明

- `apps/client` 通过 GraphQL 拉取番剧列表、搜索结果和详情
- `apps/admin` 提供后台页面和管理能力
- `packages/shared` 包含平台图标、平台 URL 配置、筛选选项、日期工具等
- `packages/tsconfig` 提供 `base.json`、`vue-app.json`、`vite.json`

## 快速开始

项目通过 `packageManager` 固定 pnpm 版本。首次使用可先启用 Corepack：

```bash
corepack enable
corepack prepare --activate
```

启动前请先在 `web/` 目录安装依赖：

```bash
cd web
pnpm install
```

### 启动前台

```bash
pnpm --filter @pjyk-web/client dev
```

### 启动后台

```bash
pnpm --filter @pjyk-web/admin dev
```

### 构建前台

```bash
pnpm --filter @pjyk-web/client build
```

### 构建后台

```bash
pnpm --filter @pjyk-web/admin build
```

### 前台 GraphQL 代码生成

`apps/client` 里有 GraphQL codegen 配置，生成产物位于 `src/graphql/generated/`。

```bash
pnpm --filter @pjyk-web/client codegen
```

## 运行约定

- 前台默认通过 `/api/graphql` 调用后端
- 公开站点的主要路由包括首页、搜索、番剧详情、番剧列表和文档页
- 后台页面依赖登录/OIDC 配置，并通过后端管理接口读写数据
