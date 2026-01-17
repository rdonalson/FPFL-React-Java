package com.financialplanner.moduleapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class responsible for setting up OpenAPI documentation for the FPFL backend services.
 * This class provides metadata about the API, such as its title, description, and version,
 * to be used in generating the OpenAPI specification.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info().title("FPFL API")
                                            .description("API documentation for FPFL backend services")
                                            .version("1.0.0"));
    }
}
