plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.netflix.dgs.codegen") version "8.7.0"
}

group = "com.yukiani"
description = "pjyk-server"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webclient")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.apache.commons:commons-text:1.15.0")
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.generateJava {
    schemaPaths = listOf("${projectDir}/src/main/resources/graphql").toMutableList()
    packageName = "com.yukiani.server.generated"
}
