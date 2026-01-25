package com.financialplanner.modulecommonbc.sanitizer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a text field or record component should be sanitized using lenient rules.
 * Lenient sanitization typically allows for a more permissive interpretation of the input,
 * such as permitting a wider range of characters or applying minimal transformations.
 *
 * This annotation can be used on fields or record components to signal the need for such
 * lenient sanitization during the processing of input data. The actual implementation of
 * lenient sanitization is determined by the sanitization logic applied in the context where
 * this annotation is used.
 *
 * Applicable targets for this annotation:
 * - Fields of a class.
 * - Record components in a record.
 *
 * Retention policy:
 * - This annotation is retained at runtime, allowing runtime-based inspection and processing.
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface LenientText {}
