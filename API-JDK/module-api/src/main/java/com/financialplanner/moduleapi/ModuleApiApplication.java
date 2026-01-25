package com.financialplanner.moduleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Module API.
 *
 * This class serves as the entry point for the Spring Boot application. It is
 * responsible for launching the application and initializing the configured
 * Spring context with the specified base package for component scanning.
 *
 * The application primarily handles module-based operations for financial
 * planning purposes and is configured to scan components and beans within the
 * "com.financialplanner" package.
 */
@SpringBootApplication(scanBasePackages = {
    "com.financialplanner.moduleapi",
    "com.financialplanner.moduleitemsbc",
    "com.financialplanner.moduledisplaybc",
    "com.financialplanner.modulecommonbc"
})
public class ModuleApiApplication {
    static void main(String[] args) {
        SpringApplication.run(ModuleApiApplication.class, args);
    }
}
