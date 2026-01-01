package com.financialplanner.moduleitemsbc.domain.exception;

public class DuplicateItemException extends DomainException {
    public DuplicateItemException(String message) { super(message); }
    public DuplicateItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
