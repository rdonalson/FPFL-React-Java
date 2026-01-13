package com.financialplanner.modulecommonbc.sanitizer;

/**
 * Provides a method to sanitize objects to remove or modify undesirable or unsafe elements
 * within the object or its structure. Useful for ensuring data conformance or security
 * by processing the object recursively if necessary.
 */
public interface Sanitizer {
    void sanitize(Object root);
}
