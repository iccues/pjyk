package com.yukiani.server;

import com.yukiani.server.config.OidcConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(OidcConfig.class)
public class PjykServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PjykServerApplication.class, args);
    }

}
