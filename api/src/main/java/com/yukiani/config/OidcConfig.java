package com.yukiani.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oidc")
public record OidcConfig(
        String issuer,
        String clientId
) {}
