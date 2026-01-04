package com.financialplanner.moduleitemsbc.domain.exception;

/**
 * Exception thrown to indicate that an attempt was made to add or register a duplicate item
 * where uniqueness is required.
 *
 * This exception is a type of {@link DomainException}, tailored for scenarios in which
 * the system encounters a violation of uniqueness constraints or business rules preventing
 * duplicate entries.
 *
 * It is typically used in the following contexts:
 * - Adding a new item into a collection or repository where the item already exists
 * - Enforcing uniqueness rules for entities (e.g., unique IDs, names, or other identifiers)
 *
 * This exception supports customization by providing a specific error message and,
 * optionally, a nested cause for more detailed debugging information.
 */
public class DuplicateItemException extends DomainException {
    public DuplicateItemException(String message) {
        super(message);
    }

    public DuplicateItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
