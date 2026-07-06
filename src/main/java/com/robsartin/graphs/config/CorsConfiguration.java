package com.robsartin.graphs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * CORS configuration for external API consumers.
 * Allows cross-origin requests from configured origins.
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Value("${app.cors.enabled:false}")
    private boolean corsEnabled;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!corsEnabled || allowedOrigins == null || allowedOrigins.isEmpty()) {
            return;
        }

        registry.addMapping("/graphs/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        registry.addMapping("/immutable-graphs/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
