package com.myname.finguard.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class RestClientTimeoutConfig {

    @Bean
    public RestClientCustomizer restClientTimeoutCustomizer(
            @Value("${app.http.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${app.http.read-timeout-ms:5000}") long readTimeoutMs
    ) {
        Duration connectTimeout = Duration.ofMillis(Math.max(connectTimeoutMs, 100));
        Duration readTimeout = Duration.ofMillis(Math.max(readTimeoutMs, 100));

        return builder -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(connectTimeout);
            factory.setReadTimeout(readTimeout);
            builder.requestFactory(factory);
        };
    }
}

