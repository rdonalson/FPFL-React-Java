package com.financialplanner.modulecommonbc.exception;

/**
 * Exception thrown to indicate an error occurred within the repository layer.
 *
 * This exception is a subclass of {@link DomainException}, representing
 * issues related to interactions with the repository, including data access, persistence,
 * or integrity violations within the domain context.
 *
 * Typical scenarios for this exception include:
 * - Failures in saving, updating, or retrieving entities from the repository
 * - Data integrity violations during repository operations
 * - Unhandled exceptions arising from repository communication or processing
 *
 * This exception allows for the inclusion of a descriptive error message and,
 * optionally, a nested cause to provide context and facilitate debugging when
 * investigating repository-related issues.
 */
public class RepositoryException extends com.financialplanner.modulecommonbc.exception.DomainException {
    public RepositoryException(String message) {
        super(message);
    }
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
