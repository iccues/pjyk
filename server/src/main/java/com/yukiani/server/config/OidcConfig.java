package com.yukiani.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 后台鉴权开关及暴露给管理端的 OIDC Client 配置。
 */
@ConfigurationProperties(prefix = "oidc")
public record OidcConfig(
        @DefaultValue("true") boolean enabled,
        String issuer,
        String clientId
) {}
