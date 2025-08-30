package com.ecclesiaflow.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@ConditionalOnProperty(name = "ecclesiaflow.auth.module.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient authWebClient(@Value("${ecclesiaflow.auth.module.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
