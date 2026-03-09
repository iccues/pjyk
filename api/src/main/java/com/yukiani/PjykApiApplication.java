package com.yukiani;

import com.yukiani.config.OidcConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(OidcConfig.class)
public class PjykApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PjykApiApplication.class, args);
    }

}
