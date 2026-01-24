package com.financialplanner.modulecommonbc.exception;

/**
 * Base class for all exceptions that represent domain-specific errors.
 * This abstract exception serves as the root for custom exceptions within the domain layer.
 * It is designed to encapsulate errors and deviations encountered in the execution of
 * business logic, rules, or constraints specific to the domain context. Domain exceptions
 * are used to indicate conditions that prevent the application from adhering to its
 * business rules or expected behavior.
 * Subclasses of this exception typically represent more refined categories of domain errors,
 * such as validation failures, repository issues, or domain rule violations. By using a
 * common base class, domain-specific exceptions can be more easily identified, categorized,
 * and handled at appropriate levels in the application.
 * This exception supports detailed error messages and optional causes, simplifying the
 * process of debugging and diagnosing domain-related issues.
 */
public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

