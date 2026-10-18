package com.yukiani.server.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * 创建访问各外部动画平台 API 的 WebClient。
 */
@Configuration
public class WebClientConfig {

    /** 建立连接的最长等待时间，单位为毫秒。 */
    private static final int CONNECT_TIMEOUT_MS = 5_000;

    /** 等待响应数据的最长时间；超时后抛出异常，避免挂起的连接无限阻塞调用线程。 */
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(30);

    @Bean
    public WebClient bangumiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.bgm.tv/v0")
                .clientConnector(connector())
                .exchangeStrategies(ExchangeStrategies.builder()
                        // 批量查询响应可能超过 WebClient 默认的 256 KiB 缓冲区。
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean
    public WebClient aniListWebClient() {
        return WebClient.builder()
                .baseUrl("https://graphql.anilist.co")
                .clientConnector(connector())
                .build();
    }

    @Bean
    public WebClient myAnimeListWebClient(@Value("${mal.client-id}") String clientId) {
        return WebClient.builder()
                .baseUrl("https://api.myanimelist.net/v2")
                .clientConnector(connector())
                .defaultHeader("X-MAL-CLIENT-ID", clientId)
                .build();
    }

    private static ReactorClientHttpConnector connector() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .responseTimeout(RESPONSE_TIMEOUT);
        return new ReactorClientHttpConnector(httpClient);
    }
}
