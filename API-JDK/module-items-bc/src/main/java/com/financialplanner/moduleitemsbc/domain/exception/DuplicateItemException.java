package com.financialplanner.moduleitemsbc.domain.exception;

/**
 * Exception thrown to indicate that an attempt to add a duplicate item has been made.
 *
 * This exception is a subclass of {@link DomainException}, used to enforce domain-specific
 * constraints that prevent the existence of duplicate items in a collection, repository,
 * or any other domain-specific context.
 *
 * Typical scenarios for this exception include:
 * - Attempting to add an entity that already exists in the system or repository
 * - Violations of domain uniqueness rules or constraints
 *
 * This exception allows for the inclusion of a descriptive error message
 * to provide clarity about the duplicate condition, as well as an optional
 * nested cause for enhanced debugging and error tracing.
 */
public class DuplicateItemException extends DomainException {
    public DuplicateItemException(String message) {
        super(message);
    }

    public DuplicateItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
