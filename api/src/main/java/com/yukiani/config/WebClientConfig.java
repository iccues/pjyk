package com.yukiani.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient bangumiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.bgm.tv/v0/subjects?type=2&sort=rank")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB
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
