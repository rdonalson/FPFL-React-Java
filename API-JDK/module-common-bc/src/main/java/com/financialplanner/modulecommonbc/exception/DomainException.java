package com.financialplanner.modulecommonbc.exception;

/**
 * Base exception for all domain-specific errors.
 *
 * This abstract class serves as a foundation for defining exception that represent
 * violations of business rules, domain invariants, or other constraints specific to
 * the domain logic. Subclasses of this exception are used to encapsulate various
 * domain-specific error scenarios.
 *
 * By inheriting from {@link RuntimeException}, this exception and its subclasses
 * allow for unchecked exception, which can propagate freely to signal issues that
 * require attention or intervention at higher levels of the application stack.
 *
 * Typical uses of this class include:
 * - Defining custom exception for different types of domain-specific errors
 * - Standardizing error handling within the domain layer
 * - Improving clarity and granularity of error types in domain-related operations
 *
 * The constructors allow for the inclusion of a detailed error message and,
 * optionally, a nested cause to provide context and facilitate debugging or error tracing.
 */
public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

