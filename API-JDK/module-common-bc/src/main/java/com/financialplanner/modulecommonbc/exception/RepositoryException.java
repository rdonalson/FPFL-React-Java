package com.financialplanner.modulecommonbc.exception;

/**
 * Exception thrown to indicate a failure in the persistence layer or during
 * interactions with a repository.
 *
 * This exception is a specialized subclass of {@link com.financialplanner.modulecommonbc.exception.DomainException}, designed
 * to encapsulate errors originating from the repository or database layer, such as
 * database access issues, constraint violations, or other persistence failures.
 *
 * Typical scenarios for this exception include:
 * - Failures in saving, updating, or deleting entities in the repository
 * - Unexpected errors during repository queries or transactions
 * - Issues caused by database errors or corruption
 *
 * This exception allows for customization by including a specific error message
 * and an optional nested cause to provide context and aid in debugging.
 */
public class RepositoryException extends com.financialplanner.modulecommonbc.exception.DomainException {
    public RepositoryException(String message) {
        super(message);
    }
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
