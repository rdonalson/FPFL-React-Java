package com.financialplanner.modulecommonbc.exception;

/**
 * Exception thrown to indicate that an illegal or inappropriate argument has been passed.
 *
 * This exception is a subclass of {@link DomainException}, tailored for scenarios where
 * an operation or method receives an argument that violates expected constraints
 * or requirements specific to the domain logic.
 *
 * Common use cases for this exception include:
 * - Detecting and signaling improper values passed to domain methods or services
 * - Enforcing validation rules on method parameters within the domain
 * - Highlighting inconsistencies in arguments that lead to invalid states
 *
 * This exception supports the inclusion of an error message that clarifies the violation
 * and, optionally, a nested cause to provide additional context for debugging and
 * tracing the source of the issue.
 */
public class IllegalArgumentException extends DomainException {
    public IllegalArgumentException(String message) {
        super(message);
    }

    public IllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
