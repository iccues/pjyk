# pjyk-server

`pjyk-server` 是有希计划的后端服务，使用 Spring Boot 构建，负责：

- 动漫主数据管理
- 外部平台映射管理
- 抓取与计算任务
- 对前端提供 GraphQL 和 REST 接口
- 认证与公开配置接口

## 技术栈

- Java 21
- Spring Boot
- Spring Data JPA
- Spring GraphQL
- Spring Security (OIDC/JWT)
- Flyway

## 快速开始

运行前请先将 `src/main/resources/application-local.yml.example` 复制为 `src/main/resources/application-local.yml`，并按需配置本地环境变量（数据库、MAL、OIDC）。

```bash
cd server
SPRING_PROFILES_ACTIVE=dev,local ./gradlew bootRun
```

说明：

- `dev,local` 表示同时启用 `application-dev.yml` 与 `application-local.yml`（以及默认的 `application.yml`）
- 一般情况下，`local` 用于覆盖本机差异配置（如数据库地址、密钥等），`dev` 放开发环境的通用配置

## 数据库

- 开发和生产使用 PostgreSQL
- 默认数据库名在仓库的 Docker 配置里是 `pjyk`
- 迁移脚本位于 `src/main/resources/db/migration`
