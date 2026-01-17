package com.financialplanner.modulecommonbc.exception;

/**
 * Exception thrown to indicate that an argument passed to a method is invalid or inappropriate
 * in the context of the domain logic.
 *
 * This exception is a subclass of {@link DomainException}, used to enforce domain-specific
 * constraints or business rules related to the validity of method arguments. It serves as a
 * mechanism to signal and standardize the handling of errors caused by improper input parameters
 * in the domain layer.
 *
 * Typical scenarios for this exception include:
 * - Arguments that fail validation against specific domain rules
 * - Parameters that violate preconditions or expected invariants
 * - Input values that are null, out of range, or otherwise inconsistent with domain logic
 *
 * This exception allows for the inclusion of a descriptive error message and an optional
 * nested cause to aid in debugging and error tracing.
 */
public class IllegalArgumentException extends DomainException {
    public IllegalArgumentException(String message) {
        super(message);
    }

    public IllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
