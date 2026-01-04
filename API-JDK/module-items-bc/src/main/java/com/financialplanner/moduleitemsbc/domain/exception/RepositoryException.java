package com.financialplanner.moduleitemsbc.domain.exception;

/**
 * Represents an exception that occurs when there is a failure at the repository layer.
 * This exception is typically used to encapsulate database access issues or other
 * persistence-related errors.
 *
 * Inherits from {@link DomainException}, allowing it to serve as a specialized
 * exception type for the domain layer with additional context on persistence failures.
 *
 * Common causes of this exception include:
 * - Database connection failures
 * - Unexpected errors in repository operations
 * - Violations of database constraints
 */
public class RepositoryException extends DomainException {
    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
