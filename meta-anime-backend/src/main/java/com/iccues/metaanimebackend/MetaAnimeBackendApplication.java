package com.iccues.metaanimebackend;

import com.iccues.metaanimebackend.config.OidcConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(OidcConfig.class)
public class MetaAnimeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetaAnimeBackendApplication.class, args);
    }

}
