package com.financialplanner.modulecommonbc.sanitizer;

/**
 * The Sanitizer interface provides methods to sanitize objects and strings.
 * Implementations of this interface are responsible for ensuring that input
 * data is cleaned or transformed to meet specific requirements or constraints.
 */
public interface Sanitizer {
    void sanitize(Object root);
    String sanitizeString(String value);
}
