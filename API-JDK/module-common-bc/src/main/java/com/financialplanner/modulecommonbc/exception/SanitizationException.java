package com.financialplanner.modulecommonbc.exception;

/**
 * Exception thrown to indicate issues encountered during the sanitization process.
 * This exception is a subclass of {@link DomainException}, representing errors that
 * occur when input data fails sanitization checks or when a sanitization operation
 * encounters invalid or problematic input. It is specifically designed to signal
 * conditions that violate the constraints or expectations defined by the application
 * during the sanitization of objects or strings.
 * Instances of this exception are typically associated with:
 * - Input data that cannot be normalized or cleaned to meet application requirements
 * - Detection of prohibited or unsafe content during sanitization
 * - Failures in applying sanitization rules to provided inputs
 * The constructor supports the inclusion of a descriptive error message, allowing for
 * precise identification of the nature and cause of the issue, facilitating debugging
 * and the implementation of corrective measures.
 */
public class SanitizationException extends DomainException {
    public SanitizationException(String message) {
        super(message);
    }
}
