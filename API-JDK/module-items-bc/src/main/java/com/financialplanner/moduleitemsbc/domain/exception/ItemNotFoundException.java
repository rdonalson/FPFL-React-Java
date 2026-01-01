package com.financialplanner.moduleitemsbc.domain.exception;

public class ItemNotFoundException extends DomainException {
    public ItemNotFoundException(String message) { super(message); }
    public ItemNotFoundException(String message, Throwable cause) { super(message, cause); }
}

