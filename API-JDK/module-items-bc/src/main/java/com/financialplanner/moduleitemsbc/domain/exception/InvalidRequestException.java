package com.financialplanner.moduleitemsbc.domain.exception;

public class InvalidRequestException extends DomainException {
    public InvalidRequestException(String message) { super(message); }
    public InvalidRequestException(String message, Throwable cause) { super(message, cause); }
}
