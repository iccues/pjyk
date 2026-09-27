package com.yukiani.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 创建访问各外部动画平台 API 的 WebClient。
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient bangumiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.bgm.tv/v0")
                .exchangeStrategies(ExchangeStrategies.builder()
                        // 批量查询响应可能超过 WebClient 默认的 256 KiB 缓冲区。
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean
    public WebClient aniListWebClient() {
        return WebClient.create("https://graphql.anilist.co");
    }

    @Bean
    public WebClient myAnimeListWebClient(@Value("${mal.client-id}") String clientId) {
        return WebClient.builder()
                .baseUrl("https://api.myanimelist.net/v2")
                .defaultHeader("X-MAL-CLIENT-ID", clientId)
                .build();
    }
}
