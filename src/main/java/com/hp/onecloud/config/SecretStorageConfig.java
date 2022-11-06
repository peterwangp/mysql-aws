package com.hp.onecloud.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
public class SecretStorageConfig {
    @Bean(name="secretStorage")
    public SecretStorage getSecretStorge() {
        return new SecretStorage();
    }

}