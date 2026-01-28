package com.financialplanner.modulecommonbc.exception;

/**
 * Exception thrown to indicate that a duplicate item exists within the domain context.
 * This exception is a subclass of {@link DomainException}, used to represent scenarios
 * where an operation fails due to the presence of an item that violates uniqueness constraints
 * or business rules. It is commonly employed in cases where creating or adding a duplicate
 * item would result in inconsistent domain state.
 * Typical scenarios for this exception include:
 * - Attempting to insert an item into a repository that already exists
 * - Violations of constraints that require unique identifiers or attributes
 * - Conflicts during operations that expect items to be distinct
 * This exception provides constructors to include a descriptive error message, with an
 * optional nested cause, allowing domain experts and developers to gain insight into
 * the nature and context of the duplication issue for debugging and error resolution.
 */
public class DuplicateItemException extends DomainException {
    public DuplicateItemException(String message) {
        super(message);
    }
    public DuplicateItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
