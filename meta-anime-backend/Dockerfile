# 多阶段构建 - 构建阶段
FROM gradle:8.14.3-jdk21 AS build

WORKDIR /app

# 复制 Gradle 配置文件
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# 下载依赖（利用 Docker 缓存层）
RUN gradle dependencies --no-daemon || true

# 复制源代码
COPY src ./src

# 构建应用（跳过测试以加快构建速度）
RUN gradle build -x test --no-daemon

# 运行阶段 - 使用更小的基础镜像
FROM eclipse-temurin:21-jre

WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=build /app/build/libs/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
