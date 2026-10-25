# pjyk-server

`pjyk-server` 是有希计划的后端服务，使用 Spring Boot 构建，负责：

- 动画主数据管理
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

## 代码注释约定

- JavaDoc 使用中文说明职责、非直观契约和关键业务规则，技术名词及代码名称保留英文
- Controller 和 GraphQL Resolver 的所有对外 API 必须提供完整 JavaDoc，覆盖全部参数、返回结果和调用方可见的异常
- 其他函数仅在签名无法表达意图、边界或副作用时添加注释；`@param`、`@return` 和 `@throws` 同样按需使用
- 重要字段和业务状态应说明单位、来源、计算方式、空值语义或行为；含义明确的普通字段无需注释
- 重写方法沿用父类契约；行内注释解释原因和取舍，不复述代码行为
- 避免作者、日期、编号式步骤和失效注释；临时禁用的任务应说明启用条件
