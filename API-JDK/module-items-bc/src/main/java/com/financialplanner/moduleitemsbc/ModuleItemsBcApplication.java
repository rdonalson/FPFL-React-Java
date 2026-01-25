package com.financialplanner.moduleitemsbc;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the ModuleItemsBcApplication.
 * This class is annotated with @SpringBootApplication, which serves as a
 * convenience annotation that adds the following:
 * - @Configuration: Marks the class as a source of bean definitions.
 * - @EnableAutoConfiguration: Enables Spring Boot’s auto-configuration mechanism.
 * - @ComponentScan: Scans the package of the annotated class for Spring components.
 * The ModuleItemsBcApplication class is responsible for bootstrapping the
 * application, initializing the Spring context, and managing the lifecycle of
 * the application.
 * To execute the application, the main method within this class is invoked,
 * which launches the embedded server and sets up the application environment.
 */
@SpringBootApplication
public class ModuleItemsBcApplication {}
