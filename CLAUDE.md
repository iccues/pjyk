# Claude 项目指南

## 项目概述

Meta Anime 是一个动漫元数据管理系统，用于统一管理动漫信息并支持多平台数据映射（如 MyAnimeList、Bangumi 等）。

## 项目架构

```
meta-anime/
├── meta-anime-backend/     # Spring Boot 后端
│   ├── src/main/java/com/iccues/metaanimebackend/
│   │   ├── controller/     # REST API 控制器
│   │   ├── service/        # 业务逻辑层
│   │   ├── entity/         # JPA 实体类
│   │   ├── dto/            # 数据传输对象
│   │   ├── mapper/         # MapStruct 映射器
│   │   ├── repository/     # JPA 仓库接口
│   │   ├── common/         # 通用类
│   │   └── config/         # 配置类
│   └── src/test/           # 单元测试
└── meta-anime-frontend/    # Vue 3 前端
    └── src/
        ├── api/            # API 调用
        ├── components/     # 组件
        ├── pages/          # 页面
        ├── router/         # 路由配置
        └── types/          # TypeScript 类型定义
```

## 技术栈详情

### 后端
- **Java 21** - 使用最新 LTS 版本
- **Spring Boot 3.5.7** - Web 框架
- **Spring Data JPA** - 数据持久层
- **Spring WebFlux** - 响应式 HTTP 客户端（用于调用外部 API）
- **PostgreSQL** - 生产数据库
- **H2** - 测试数据库
- **MapStruct 1.6.3** - 对象映射
- **Lombok** - 简化代码

### 前端
- **Vue 3** - 使用 Composition API
- **TypeScript** - 类型安全
- **Vite 7** - 构建工具
- **Element Plus** - UI 组件库
- **TailwindCSS 4** - 样式框架
- **Vue Router** - 路由管理

## 核心实体

### Anime（动漫）
- 主要的动漫信息实体
- 包含标题（AnimeTitles）、日期范围等
- 使用乐观锁（@Version）

### Mapping（映射）
- 动漫与外部平台的 ID 映射关系
- 支持多平台（MAL、Bangumi 等）
- 使用乐观锁（@Version）

## API 结构

### 前台 API
- `AnimeController` - 动漫查询接口

### 管理后台 API
- `AdminAnimeController` - 动漫管理（CRUD）
- `AdminMappingController` - 映射管理（CRUD）
- `AdminFetchController` - 外部数据抓取

## 开发规范

### 后端规范
1. **DTO 转换**：使用 MapStruct 自动映射，避免手动转换
2. **事务管理**：在 Service 层使用 `@Transactional` 注解
3. **乐观锁**：Anime 和 Mapping 实体使用 `@Version` 字段
4. **统一响应**：使用 `Response<T>` 封装 API 响应

### 前端规范
1. **组件命名**：使用 PascalCase
2. **API 调用**：统一放在 `src/api/` 目录
3. **类型定义**：放在 `src/types/` 目录

## 常用命令

```bash
# 后端运行
cd meta-anime-backend && ./gradlew bootRun

# 后端测试
cd meta-anime-backend && ./gradlew test

# 后端构建
cd meta-anime-backend && ./gradlew build

# 前端开发
cd meta-anime-frontend && npm run dev

# 前端构建
cd meta-anime-frontend && npm run build
```

## 配置文件

### 后端配置
- `application.yml` - 主配置文件
- `gradle.properties` - Gradle 配置

### 环境变量
- `MAL_CLIENT_ID` - MyAnimeList API 客户端 ID

## 数据库

- **开发/生产**：PostgreSQL，数据库名 `meta-anime`
- **测试**：H2 内存数据库
- **DDL 策略**：`hibernate.ddl-auto: update`

## 注意事项

1. 后端需要 JDK 21，确保 IDEA 和 Gradle 都配置了正确的 JDK 版本
2. Gradle 使用阿里云镜像加速依赖下载
3. 前端开发时后端需要同时运行（跨域已配置）
