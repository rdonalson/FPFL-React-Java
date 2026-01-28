package com.financialplanner.moduleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the ModuleApiApplication, enabling it as a Spring Boot application.
 * This application performs a component scan over the specified base packages to
 * discover and register Spring components, configurations, and services.
 * Specifically, the following base packages are scanned:
 * - com.financialplanner.modulecommonbc
 * - com.financialplanner.moduleitemsbc
 * - com.financialplanner.moduledisplaybc
 * - com.financialplanner.moduleapi
 * It is designed to initiate and bootstrap the application using the Spring framework.
 */
@SpringBootApplication(scanBasePackages = {
    "com.financialplanner.modulecommonbc",
    "com.financialplanner.moduleitemsbc",
    "com.financialplanner.moduledisplaybc",
    "com.financialplanner.moduleapi"
})
public class ModuleApiApplication {
    static void main(String[] args) {
        SpringApplication.run(ModuleApiApplication.class, args);
    }
}
