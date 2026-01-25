package com.financialplanner.modulecommonbc.sanitizer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a text field or record component should be sanitized using strict rules.
 * Strict sanitization enforces more restrictive transformations or validations, ensuring
 * that the input adheres to stringent standards. This could include removing or escaping
 * disallowed characters, normalizing the string, or applying other rigorous constraints.
 *
 * This annotation can be used on fields or record components to signal the need for such
 * strict sanitization during the processing of input data. The actual implementation of
 * strict sanitization is determined by the sanitization logic applied in the context
 * where this annotation is used.
 *
 * Applicable targets:
 * - Fields of a class
 * - Record components in a record
 *
 * Retention policy:
 * - Retained at runtime to enable runtime-based inspection and processing.
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrictText {}
