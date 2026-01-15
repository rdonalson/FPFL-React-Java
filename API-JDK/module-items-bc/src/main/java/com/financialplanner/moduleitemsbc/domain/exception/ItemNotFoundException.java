package com.financialplanner.moduleitemsbc.domain.exception;

/**
 * Exception thrown to indicate that a specific item could not be found.
 *
 * This exception is a type of {@link DomainException}, intended to signal situations
 * where an operation or request requires the presence of an item that does not exist
 * or cannot be located.
 *
 * Typical scenarios for this exception include:
 * - Attempting to retrieve a nonexistent entity from a repository or collection
 * - Operations that depend on the existence of an item, which is missing
 *
 * This exception allows for the inclusion of a descriptive error message and, optionally,
 * a nested cause to provide additional context for debugging and error tracking.
 */
public class ItemNotFoundException extends DomainException {
    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

