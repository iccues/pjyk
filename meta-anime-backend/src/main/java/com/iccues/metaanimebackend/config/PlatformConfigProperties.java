package com.iccues.metaanimebackend.config;

import com.iccues.metaanimebackend.entity.Platform;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 平台配置属性，绑定 application.yml 中的 platform.* 配置
 */
@Configuration
@ConfigurationProperties(prefix = "platform")
@Data
public class PlatformConfigProperties {
    private PlatformConfig bangumi = new PlatformConfig();
    private PlatformConfig aniList = new PlatformConfig();
    private PlatformConfig myAnimeList = new PlatformConfig();

    /**
     * 根据平台类型获取对应的配置
     */
    public PlatformConfig getConfig(Platform platform) {
        return switch (platform) {
            case Bangumi -> bangumi;
            case AniList -> aniList;
            case MyAnimeList -> myAnimeList;
        };
    }
}
