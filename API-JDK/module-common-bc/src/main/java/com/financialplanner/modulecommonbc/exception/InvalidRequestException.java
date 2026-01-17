package com.financialplanner.modulecommonbc.exception;

/**
 * Exception thrown to indicate that a request within the domain context is invalid.
 *
 * This exception is a subclass of {@link DomainException}, and it is used to represent
 * scenarios where a request or input violates domain-specific constraints, rules,
 * or expectations. It serves as a means to standardize the handling of errors caused
 * by invalid or malformed requests in the domain layer.
 *
 * Typical scenarios for this exception include:
 * - Requests with missing or incorrect required parameters
 * - Violations of domain rules or policies by the request
 * - Operations that fail due to logical inconsistencies in the request data
 *
 * This exception provides constructors to include a descriptive error message,
 * with an optional nested cause, enabling developers and domain experts to trace
 * and analyze the source of the issue for debugging and resolution purposes.
 */
public class InvalidRequestException extends com.financialplanner.modulecommonbc.exception.DomainException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
