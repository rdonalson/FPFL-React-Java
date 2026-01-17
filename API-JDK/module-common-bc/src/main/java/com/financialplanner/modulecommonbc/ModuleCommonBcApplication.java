package com.financialplanner.modulecommonbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.lang.module.Configuration;

/**
 * Entry point for the ModuleCommonBcApplication.
 * <p>
 * This class serves as the main entry point for the Spring Boot application.
 * It is annotated with {@link SpringBootApplication}, which acts as a convenience annotation
 * that combines {@link Configuration}, {@link EnableAutoConfiguration}, and {@link ComponentScan}.
 * <p>
 * The {@code main} method uses {@link SpringApplication#run(Class, String...)} to launch the application.
 */
@SpringBootApplication
public class ModuleCommonBcApplication {
    static void main(String[] args) {
        SpringApplication.run(ModuleCommonBcApplication.class, args);
    }
}
