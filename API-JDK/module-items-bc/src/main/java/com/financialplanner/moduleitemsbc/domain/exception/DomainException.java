package com.financialplanner.moduleitemsbc.domain.exception;

/**
 * Represents an exception specific to domain-level operations or business logic rules.
 *
 * This abstract exception serves as the base class for more specific domain-related
 * exceptions, such as violations of business rules, invalid requests, item not found,
 * or persistence layer failures. Subclasses of this exception allow differentiation
 * and specialization for various domain error scenarios.
 *
 * Key characteristics include:
 * - Encapsulation of domain-specific error messages or conditions
 * - Support for nested exceptions to provide additional debugging context
 */
public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

