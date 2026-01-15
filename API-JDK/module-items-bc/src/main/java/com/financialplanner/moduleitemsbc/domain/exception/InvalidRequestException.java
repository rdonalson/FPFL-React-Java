package com.financialplanner.moduleitemsbc.domain.exception;

/**
 * Exception thrown to indicate that an invalid request has been made, violating
 * certain business or domain-specific rules and constraints.
 *
 * This exception is a type of {@link DomainException}, designed to signal
 * issues arising from improper or malformed requests within the domain's context.
 * It serves as a mechanism to enforce domain invariants and constraints.
 *
 * Common scenarios for this exception include:
 * - Requests that fail validation or violate business requirements
 * - Operations that are disallowed under the current state or rules
 * - Requests containing invalid or inconsistent data
 *
 * This exception allows for the inclusion of a descriptive error message and,
 * optionally, a nested cause for enhanced debugging and error tracing.
 */
public class InvalidRequestException extends DomainException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
