package com.financialplanner.modulecommonbc.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The domain exceptions the API maps to HTTP statuses keep their message and cause. */
class ExceptionsTest {

    @Test
    void domainExceptionsCarryTheirMessage() {
        assertEquals("Item 4 not found", new ItemNotFoundException("Item 4 not found").getMessage());
        assertEquals("bad", new DomainValidationException("bad").getMessage());
        assertEquals("dup", new DuplicateItemException("dup").getMessage());
        assertEquals("nope", new SanitizationException("nope").getMessage());
        assertEquals("forbidden", new ForbiddenOperationException("forbidden").getMessage());
        assertEquals("creds", new InvalidCredentialsException("creds").getMessage());
        assertInstanceOf(DomainException.class, new ItemNotFoundException("x"));
        assertInstanceOf(RuntimeException.class, new ForbiddenOperationException("x"));
    }

    @Test
    void wrappingExceptionsKeepTheCause() {
        Throwable cause = new IllegalStateException("db down");
        assertSame(cause, new RepositoryException("Database failure", cause).getCause());
        assertSame(cause, new DuplicateItemException("dup", cause).getCause());
    }
}
