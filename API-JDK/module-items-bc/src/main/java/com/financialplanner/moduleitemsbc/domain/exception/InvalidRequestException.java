package com.financialplanner.moduleitemsbc.domain.exception;

/**
 * Exception thrown to indicate that a request made to the system is invalid or cannot be processed.
 *
 * This exception is a specialized type of {@link DomainException}, which represents domain-specific
 * errors. The InvalidRequestException is typically used for scenarios where a client's input
 * or action does not comply with the required business rules or validation criteria.
 *
 * Common use cases for this exception include:
 * - Validating inputs that fail constraints (e.g., missing required fields or invalid formats)
 * - Handling unauthorized or inappropriate requests within the application's domain logic
 *
 * This exception allows for providing a custom error message, and optionally, a root cause
 * (an additional exception) to aid in debugging and troubleshooting.
 */
public class InvalidRequestException extends DomainException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
