package com.financialplanner.moduleitemsbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The ModuleItemsBcApplication class initializes and launches the Spring Boot application.
 * This class is the main entry point of the application.
 */
@SpringBootApplication
public class ModuleItemsBcApplication {

    /**
     * The main method serves as the entry point for the Spring Boot application.
     * It bootstraps the application by invoking the SpringApplication.run method.
     *
     * @param args an array of command-line arguments passed during the application startup
     */
    static void main(String[] args) {
        SpringApplication.run(ModuleItemsBcApplication.class, args);
    }
}
