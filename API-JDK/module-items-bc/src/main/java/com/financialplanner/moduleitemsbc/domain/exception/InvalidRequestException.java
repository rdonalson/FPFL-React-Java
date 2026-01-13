package com.financialplanner.moduleitemsbc.domain.exception;

/**
 * Exception thrown to indicate that a request made to the system is invalid or
 * does not conform to expected requirements.
 * ---
 * This exception is a specialized subclass of {@link DomainException}, designed
 * to represent invalid request scenarios where the input or operation requested
 * by the user violates business rules, constraints, or logical requirements.
 * ---
 * Typical use cases for this exception include:
 * - Validation failures for user input or system requests
 * - Requests that fail to meet preconditions or required criteria
 * - Attempts to perform operations that are logically inconsistent
 * --
 * Provides support for including a detailed error message and an optional
 * nested exception to assist with debugging and tracing the root cause.
 */
public class InvalidRequestException extends DomainException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
