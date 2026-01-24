package com.financialplanner.modulecommonbc.exception;

/**
 * Exception thrown to indicate that a specific item could not be found within the domain context.
 * This exception is a subclass of {@link DomainException}, representing scenarios where
 * an operation fails because a required or expected item does not exist. It is used to signal
 * the absence of an item that is critical to the successful execution of a business operation
 * or rule within the domain layer.
 * Typical scenarios for this exception include:
 * - Attempts to retrieve a non-existing entity from a repository
 * - Business operations requiring the presence of a specific item that is missing
 * - Violations of assumptions regarding the existence of certain items in the domain
 * This exception provides constructors to include a descriptive error message, with an
 * optional nested cause, allowing developers and domain experts to trace the source and
 * context of the error for debugging and resolution.
 */
public class ItemNotFoundException extends DomainException {
    public ItemNotFoundException(String message) {
        super(message);
    }
}

