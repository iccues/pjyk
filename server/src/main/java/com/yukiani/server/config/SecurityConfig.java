package com.yukiani.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 根据 OIDC 开关配置后台鉴权，默认保护管理 API。
 */
@Configuration
public class SecurityConfig {

    /**
     * 启用鉴权时保护管理 API；关闭时放行所有请求且不解析 Bearer Token。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OidcConfig oidc) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        if (oidc.enabled()) {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/anime/**").permitAll()
                            .requestMatchers("/api/config/**").permitAll()
                            .requestMatchers("/api/graphiql").permitAll()
                            .requestMatchers("/api/graphql").permitAll()
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth -> oauth
                            .jwt(jwt -> {}));
        } else {
            http.authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll())
                    .oauth2ResourceServer(AbstractHttpConfigurer::disable);
        }

        return http.build();
    }
}
