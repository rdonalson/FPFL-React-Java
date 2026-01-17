package com.financialplanner.modulecommonbc.exception;

/**
 * Base class for exceptions that occur within the domain layer.
 *
 * This abstract exception serves as the foundational type for all domain-specific
 * exceptions, enabling a consistent approach to error handling and propagation
 * within the domain logic. Subclasses of this exception represent specific types
 * of errors or violations related to business rules, constraints, or logic
 * specific to the domain model.
 *
 * Common use cases for exceptions extending this class include:
 * - Violations of domain invariants or rules
 * - Errors in repository interactions or data integrity
 * - Invalid or unsupported domain-specific operations
 *
 * This class provides constructors to define a descriptive error message and,
 * optionally, a nested cause, enabling effective debugging and error traceability.
 */
public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

