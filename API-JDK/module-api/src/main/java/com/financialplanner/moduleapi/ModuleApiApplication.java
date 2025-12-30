package com.financialplanner.moduleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ModuleApiApplication is the entry point for the Spring Boot application.
 * It is responsible for bootstrapping and initializing the application
 * by scanning the specified base packages.
 * ---
 * The base packages included for scanning are:
 * - com.financialplanner.moduleapi
 * - com.financialplanner.moduleitemsbc
 * ---
 * This class enables the auto-detection of components, configurations,
 * and services within the defined base packages and initiates the application's
 * lifecycle.
 */
@SpringBootApplication(scanBasePackages = {
    "com.financialplanner.moduleapi",
    "com.financialplanner.moduleitemsbc"
})
public class ModuleApiApplication {
    /**
     * The main method serves as the entry point of the application.
     * It initializes and runs the Spring Boot application.
     *
     * @param args an array of command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ModuleApiApplication.class, args);
    }
}
