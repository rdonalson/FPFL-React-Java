package com.financialplanner.moduleapi.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This configuration class sets up web-related settings for the application.
 * It primarily focuses on configuring Cross-Origin Resource Sharing (CORS)
 * to enable secure communication between the application and other domains.
 */
@Configuration
public class WebConfig {
    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings for the application,
     * allowing all origins and enabling specific HTTP methods such as GET, PUT, POST,
     * PATCH, and DELETE.
     * ---
     *
     * @return A {@code WebMvcConfigurer} instance that defines the CORS configuration for the application.
     */
    @Bean
    public WebMvcConfigurer corsConfigure() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "PUT", "POST", "PATCH", "DELETE");
            }
        };
    }
}


