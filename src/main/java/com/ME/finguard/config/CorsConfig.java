package com.yourname.finguard.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.security.cors.allowed-origins:}") String allowedOriginsRaw
    ) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        List<String> allowedOrigins = parseOrigins(allowedOriginsRaw);
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        log.info("CORS allowed origins (parsed): {}", allowedOrigins);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> parseOrigins(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        // Support YAML list rendered as comma-separated and trim spaces
        return List.of(raw.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
