package com.financialplanner.modulecommonbc.sanitizer;

public interface Sanitizer {

    /**
     * Sanitizes an entire object graph in-place (records return new instances).
     * Applies annotation-driven strict/lenient/URI rules.
     */
    void sanitize(Object root);

    /**
     * Sanitizes a single string using strict rules by default.
     * Useful for manual calls or utility use.
     */
    String sanitizeString(String value);
}
