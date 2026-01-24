package com.financialplanner.modulecommonbc.exception;

/**
 * Exception thrown to indicate a validation failure within the domain context.
 * This exception is a subclass of {@link DomainException}, designed to represent
 * errors that occur when input data, state, or parameters fail to satisfy the
 * defined validation rules or constraints of the domain.
 * Typical scenarios for this exception include:
 * - Violations of business rules or domain-specific constraints
 * - Input data that does not conform to expected formats or values
 * - Attempts to perform operations on invalid or inconsistent state
 * This exception provides constructors to include a detailed error message
 * describing the nature of the validation failure. It is intended to be used
 * in cases where validation errors prevent the successful execution of domain
 * operations, enabling developers to identify and handle such errors at an
 * appropriate level within the application.
 */
public class DomainValidationException extends DomainException {
    public DomainValidationException(String message) {
        super(message);
    }
}
