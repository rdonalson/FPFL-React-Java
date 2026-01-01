package com.financialplanner.moduleitemsbc.domain.exception;

public class RepositoryException extends DomainException {
    public RepositoryException(String message) { super(message); }
    public RepositoryException(String message, Throwable cause) {  super(message, cause); }
}
