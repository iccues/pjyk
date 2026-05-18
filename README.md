# 有希计划 Project Yuki

有希计划现已部署在 [`www.yukiani.com`](https://www.yukiani.com)。

有希计划（Project Yuki）是一个番剧综合评分平台，整合 Bangumi、MyAnimeList、AniList 的评分与热度数据，提供更客观、更全面的番剧评价参考。项目按职责拆成三个部分：

- `api`：后端服务，负责动漫数据、映射关系、抓取任务、GraphQL 查询和管理后台接口
- `web`：前端 monorepo，包含前台 `apps/client`、后台 `apps/admin`，以及共享包 `packages/shared`、`packages/tsconfig`
- `analysis`：数据分析工具，直接连数据库读取评分和热度数据，输出统计结果

## 目录概览

```text
.
├── api/        Spring Boot 后端
├── web/        pnpm monorepo 前端
├── analysis/   Python 数据分析脚本
├── docker-compose.yml
├── .env.example
└── README.md
```

## 快速开始

1. 复制根目录 `.env.example` 为 `.env`，并填写 Docker 需要的变量。
2. 使用 Docker 一键启动整套服务：

```bash
docker compose up --build
```

## 各模块文档

- [后端说明](./api/README.md)
- [前端说明](./web/README.md)
- [数据分析说明](./analysis/README.md)

## 说明

- 数据库：必须使用 PostgreSQL（后端依赖 `pg_trgm` 扩展），默认数据库名为 `pjyk`；后端和数据分析模块都依赖该数据库
- 后端默认 GraphQL 路径是 `/api/graphql`
- 前端通过 `/api/graphql` 和后端交互，`apps/client` 是公开站点，`apps/admin` 是管理后台
- 数据分析模块默认连接本地 PostgreSQL
- [`pjyk-labs`](https://github.com/iccues/pjyk-labs)：`pjyk` 的实验仓库，用于尝试其他技术栈的实现方式
