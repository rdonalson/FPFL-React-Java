package com.financialplanner.moduleitemsbc.domain.exception;


public class IllegalArgumentException extends DomainException {
    public IllegalArgumentException(String message) {
        super(message);
    }

    public IllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
