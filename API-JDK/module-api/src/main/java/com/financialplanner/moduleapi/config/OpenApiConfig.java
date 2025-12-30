package com.financialplanner.moduleapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class responsible for setting up OpenAPI documentation for the FPFL API.
 * This class defines the metadata and details that describe the available endpoints in the API.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Configures and provides an instance of the OpenAPI object, which
     * includes metadata information about the FPFL API, such as its title,
     * description, and version.
     * ---
     * @return an OpenAPI instance containing API documentation details including title,
     *         description, and version information.
     */
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
            .info(new Info()
                .title("FPFL API")
                .description("API documentation for FPFL backend services")
                .version("1.0.0"));
    }
}
