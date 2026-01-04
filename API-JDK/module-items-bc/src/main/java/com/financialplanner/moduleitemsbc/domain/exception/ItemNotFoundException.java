package com.financialplanner.moduleitemsbc.domain.exception;

/**
 * Exception thrown when an expected item is not found in the system or repository.
 *
 * This exception indicates that an operation which assumed the existence of a specific
 * item failed because the item could not be located. It is a specialized type of
 * {@link DomainException} dedicated to representing item-not-found scenarios.
 *
 * Common causes for this exception include:
 * - Attempting to retrieve a non-existent entity by its ID
 * - Operations that require an existing item but the item was removed or never added
 *
 * This exception supports providing additional context through a custom message
 * and an optional nested cause for more detailed debugging information.
 */
public class ItemNotFoundException extends DomainException {
    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

