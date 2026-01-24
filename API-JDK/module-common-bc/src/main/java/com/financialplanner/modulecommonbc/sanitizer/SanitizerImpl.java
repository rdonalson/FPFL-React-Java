package com.financialplanner.modulecommonbc.sanitizer;

import com.financialplanner.modulecommonbc.exception.SanitizationException;
import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;
import com.financialplanner.modulecommonbc.sanitizer.annotations.StrictText;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Provides an implementation for sanitizing various types of data, including strings,
 * URIs, collections, records, and POJOs. The class defines a set of methods to
 * apply normalization, sanitization, and validation rules to ensure input data
 * adheres to predefined constraints.
 * The class includes utilities for handling nested structures, cyclical references,
 * and different data types, ensuring a comprehensive sanitization process.
 * Fields:
 * - LOG: Logger for capturing processing details and potential issues.
 * - STRICT_TEXT: Predefined pattern for validating strict text rules.
 * - URI_PATTERN: Predefined pattern for sanitizing and validating URI strings.
 * Methods:
 * - normalize(String): Normalizes a given string by removing unwanted characters,
 * invisible characters, and whitespace formatting issues.
 * - sanitizeStrictText(String): Sanitizes a string by enforcing compliance with
 * strict text rules and throws an exception if invalid input is detected.
 * - sanitizeLenientText(String): Sanitizes a string in a lenient manner by applying
 * normalization rules without strict validation.
 * - sanitizeUri(String): Sanitizes and validates a URI string against predefined rules
 * and throws an exception for invalid input.
 * - sanitize(Object): Recursively sanitizes a given object and its nested structures
 * according to specific rules while handling cyclical references.
 * - sanitizeString(String): Sanitizes an input string by enforcing strict validation
 * rules and leveraging the underlying methods for normalization.
 * - sanitizeValue(Object, Set<Object>): Recursively sanitizes and processes a given value
 * based on its type, with support for various data structures and types.
 * - sanitizePojoFields(Object, Class<?>, Set<Object>): Processes non-static and non-transient
 * fields of a given POJO object, applying sanitization recursively while tracking visited objects.
 * - sanitizeRecord(Object, Class<?>, Set<Object>): Applies sanitization rules to a record and its
 * components, creating a sanitized instance if changes are needed, and avoiding cyclic references.
 * - looksLikeUri(String): Determines whether a given string appears to be a URI based on specific
 * patterns like a slash, "http://", or "https://".
 * - getAllFields(Class<?>): Retrieves all declared fields of a class and its superclasses, excluding {@code Object}.
 * - isPrimitiveOrWrapper(Class<?>): Checks if a given class represents a primitive type or its wrapper type.
 * - sanitizeCollection(Collection<?>): Sanitizes the elements of a provided collection by iterating over
 * each element and applying type-appropriate sanitization rules.
 */
@Component
public class SanitizerImpl implements Sanitizer {

    private static final Logger LOG = Logger.getLogger(SanitizerImpl.class.getName());

    // ============================================================
    //  Layer A: Normalization
    // ============================================================
    /**
     * A precompiled regular expression pattern used to enforce strict textual content sanitization.
     * This pattern matches strings that consist only of Unicode letters, numbers, spaces, and the
     * following additional characters: hyphen (-), underscore (_), period (.) and apostrophe (').
     * This ensures that input text adheres to a strict, predefined character set suitable for secure
     * and controlled environments, rejecting any characters outside this allowable set.
     * The pattern is typically used in conjunction with methods that validate or sanitize input strings
     * to comply with specific text constraints.
     */
    private static final Pattern STRICT_TEXT = Pattern.compile("[\\p{L}0-9 \\-_'.]*");

    // ============================================================
    //  Layer B: Strict Validation
    // ============================================================
    /**
     * A compiled regular expression pattern used to validate or match URI strings.
     * The pattern allows a wide range of alphanumeric characters and symbols
     * typically found in URIs, including:
     * - Letters (uppercase and lowercase)
     * - Digits
     * - Common URI components such as `:`, `/`, `?`, `&`, `=`, `.`, `_`, `#`, `%`, `-`, and `+`
     * This pattern is useful for ensuring that strings conform to expected URI formats
     * during sanitization processes.
     */
    private static final Pattern URI_PATTERN = Pattern.compile("[A-Za-z0-9:/?&=._#%\\-+]*");

    /**
     * Normalizes the input string by removing unwanted characters and formatting it to meet
     * specific text sanitization rules. The method performs the following operations on the input:
     * - Removes Byte Order Marks (BOM).
     * - Removes zero-width and invisible characters.
     * - Removes control characters.
     * - Trims leading and trailing whitespace.
     * - Collapses multiple consecutive spaces into a single space.
     *
     * @param s the input string to normalize; can be null
     *
     * @return a normalized string with unwanted characters removed and formatting adjusted,
     * or null if the input string is null
     */
    private static String normalize(String s) {
        if (s == null) {
            return null;
        }

        return s.replace("\uFEFF", "")                             // BOM
                .replaceAll("[\\u200B-\\u200F\\u202A-\\u202E]", "") // zero-width/invisible
                .replaceAll("\\p{Cc}", "")                         // control chars
                .trim()
                .replaceAll("\\s{2,}", " ");                       // collapse whitespace
    }

    // ============================================================
    //  Layer C: Lenient Text (free-form)
    // ============================================================

    /**
     * Sanitizes the input string by enforcing strict text rules. This method validates the
     * string against a predefined strict text pattern. If the input string contains
     * invalid characters that do not match the strict text pattern, an exception is thrown.
     * The input string is normalized before validation.
     *
     * @param s the input string to sanitize; can be null
     *
     * @return the sanitized string if it matches the strict text pattern,
     * or null if the input string is null
     *
     * @throws SanitizationException if the input string contains invalid characters
     */
    private static String sanitizeStrictText(String s) {
        s = normalize(s);
        if (s == null) {
            return null;
        }

        if (!STRICT_TEXT.matcher(s)
                        .matches()) {
            throw new SanitizationException("Invalid characters in string");
        }
        return s;
    }

    // ============================================================
    //  URI Sanitization (structured)
    // ============================================================

    /**
     * Sanitizes the input string in a lenient manner by applying normalization rules
     * without strict validation. This method leverages the {@code normalize} method
     * to clean and format the text by removing unwanted characters, trimming whitespace,
     * and collapsing multiple spaces. It is designed for use cases that do not require
     * strict conformance to specific patterns.
     *
     * @param s the input string to sanitize; can be null
     *
     * @return a sanitized string with normalization rules applied, or null if the input string is null
     */
    private static String sanitizeLenientText(String s) {
        return normalize(s);
    }

    /**
     * Sanitizes a URI string by normalizing it and validating it against a predefined URI pattern.
     * The method ensures that the input string meets specific URI sanitization requirements and
     * throws an exception if invalid characters are detected. If the input is null, the method
     * returns null.
     *
     * @param s the URI string to sanitize; can be null
     *
     * @return the sanitized URI string if it matches the required pattern,
     * or null if the input string is null
     *
     * @throws SanitizationException if the input string contains invalid characters
     *                               that do not match the URI pattern
     */
    private static String sanitizeUri(String s) {
        s = normalize(s);
        if (s == null) {
            return null;
        }

        if (!URI_PATTERN.matcher(s)
                        .matches()) {
            throw new SanitizationException("Invalid characters in URI string");
        }
        return s;
    }

    // ============================================================
    //  Public API
    // ============================================================

    /**
     * Retrieves all fields declared in the specified class and its superclasses up to, but not including,
     * {@code Object}. This method collects both private and public fields, as well as fields with other modifiers,
     * from the specified class hierarchy.
     *
     * @param type the class whose fields are to be retrieved; must not be null
     *
     * @return a list of {@code Field} objects representing all declared fields of the specified class and its
     * superclasses, excluding {@code Object}
     */
    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * Recursively sanitizes the provided object and its nested structures.
     * This method ensures that the input object is cleaned according to specific
     * sanitization rules. It handles various types of objects, including strings,
     * collections, maps, arrays, records, and POJOs.
     * Objects that have already been visited are skipped to prevent cyclical processing.
     *
     * @param root the root object to sanitize; can be null
     */
    @Override
    public void sanitize(Object root) {
        if (root == null) {
            return;
        }
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        sanitizeValue(root, visited);
    }

    // ============================================================
    //  Core Recursive Sanitization
    // ============================================================

    /**
     * Sanitizes the input string by performing strict validation against a predefined text pattern.
     * This method leverages the underlying sanitizeStrictText method to normalize the input and
     * validate its conformance to strict text sanitization rules.
     *
     * @param value the input string to sanitize; can be null
     *
     * @return the sanitized string if it matches the strict text pattern,
     * or null if the input string is null
     *
     * @throws SanitizationException if the input string contains invalid characters
     */
    @Override
    public String sanitizeString(String value) {
        return sanitizeStrictText(value);
    }

    // ============================================================
    //  Annotation Routing for POJO Fields
    // ============================================================

    /**
     * Recursively sanitizes and processes the provided input value based on its type.
     * This method supports various types including primitives, collections, maps, arrays,
     * strings, records, and POJOs. Specific sanitization rules are applied to sanitize
     * the value and its nested structures while ensuring visited objects are skipped to
     * prevent cyclical processing.
     *
     * @param value   the value to sanitize; can be null
     * @param visited a set of already visited objects to prevent cyclic references during sanitization
     *
     * @return the sanitized value or the original value if no sanitization was needed,
     * or null if the input value is null
     */
    private Object sanitizeValue(Object value, Set<Object> visited) {
        if (value == null) {
            return null;
        }

        Class<?> clazz = value.getClass();

        if (isPrimitiveOrWrapper(clazz) || clazz.isEnum()) {
            return value;
        }

        if (visited.contains(value)) {
            return value;
        }
        visited.add(value);

        // --- String routing (default strict) ---
        switch (value) {
            case String s -> {
                if (looksLikeUri(s)) {
                    return sanitizeUri(s);
                }
                return sanitizeStrictText(s);
            }

            // --- Collections ---
            case Collection<?> col -> {
                sanitizeCollection(col, visited);
                return value;
            }

            // --- Maps ---
            case Map<?, ?> map -> {
                sanitizeMap(map, visited);
                return value;
            }
            default -> {
            }
        }

        // --- Arrays ---
        if (clazz.isArray()) {
            sanitizeArray(value, visited);
            return value;
        }

        // --- Records ---
        if (clazz.isRecord()) {
            Object newRecord = sanitizeRecord(value, clazz, visited);
            return newRecord != null ? newRecord : value;
        }

        // --- POJOs ---
        sanitizePojoFields(value, clazz, visited);
        return value;
    }

    // ============================================================
    //  Record Component Annotation Routing
    // ============================================================

    /**
     * Recursively sanitizes the fields of a given POJO object. The method inspects all
     * fields of the provided class and processes each non-static and non-transient field.
     * Strings are sanitized based on field-specific annotations or patterns, while
     * non-string fields are recursively sanitized. Already visited objects are tracked
     * to avoid cyclical processing.
     *
     * @param target  the target object whose fields will be sanitized; can be null
     * @param clazz   the class definition of the target object, used to retrieve field metadata
     * @param visited a set of already visited objects to prevent cyclic references during sanitization
     */
    private void sanitizePojoFields(Object target, Class<?> clazz, Set<Object> visited) {
        for (Field field : getAllFields(clazz)) {

            int mods = field.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object old = field.get(target);
                Object replaced;

                if (old instanceof String s) {

                    if (field.isAnnotationPresent(LenientText.class)) {
                        replaced = sanitizeLenientText(s);

                    }
                    else if (field.isAnnotationPresent(StrictText.class)) {
                        replaced = sanitizeStrictText(s);

                    }
                    else if (looksLikeUri(s)) {
                        replaced = sanitizeUri(s);

                    }
                    else {
                        replaced = sanitizeStrictText(s);
                    }

                }
                else {
                    replaced = sanitizeValue(old, visited);
                }

                if (replaced != old) {
                    try {
                        field.set(target, replaced);
                    } catch (Exception e) {
                        LOG.log(Level.FINE, "Cannot set field " + field.getName(), e);
                    }
                }

            } catch (SanitizationException iae) {
                throw iae;
            } catch (Exception e) {
                LOG.log(Level.FINE, "Error accessing field " + field.getName(), e);
            }
        }
    }

    // ============================================================
    //  Helpers
    // ============================================================

    /**
     * Sanitizes a record by applying specific sanitization rules to its components.
     * Each component is inspected, and based on its type and annotations, appropriate
     * sanitization is performed. If changes are made, a new sanitized record instance
     * is created and returned. The method handles recursive sanitization for nested
     * objects while avoiding cyclic references.
     *
     * @param record  the record object to sanitize; must not be null
     * @param clazz   the class type of the record being sanitized; must not be null
     * @param visited a set of already visited objects to prevent cyclic references during sanitization; must not be
     *                null
     *
     * @return the sanitized record object if changes are made, or the original record object if no changes are needed
     */
    private Object sanitizeRecord(Object record, Class<?> clazz, Set<Object> visited) {
        try {
            RecordComponent[] components = clazz.getRecordComponents();
            Object[] args = new Object[components.length];
            boolean changed = false;

            for (int i = 0; i < components.length; i++) {
                RecordComponent rc = components[i];
                Method accessor = rc.getAccessor();
                Object old = accessor.invoke(record);
                Object replaced;

                if (old instanceof String s) {

                    if (rc.isAnnotationPresent(LenientText.class)) {
                        replaced = sanitizeLenientText(s);

                    }
                    else if (rc.isAnnotationPresent(StrictText.class)) {
                        replaced = sanitizeStrictText(s);

                    }
                    else if (looksLikeUri(s)) {
                        replaced = sanitizeUri(s);

                    }
                    else {
                        replaced = sanitizeStrictText(s);
                    }

                }
                else {
                    replaced = sanitizeValue(old, visited);
                }

                args[i] = replaced;
                if (replaced != old) {
                    changed = true;
                }
            }

            if (!changed) {
                return record;
            }

            Constructor<?> ctor = clazz.getDeclaredConstructor(Arrays.stream(components)
                                                                     .map(RecordComponent::getType)
                                                                     .toArray(Class[]::new));
            ctor.setAccessible(true);
            return ctor.newInstance(args);

        } catch (SanitizationException iae) {
            throw iae;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to sanitize record " + clazz.getName(), e);
            return null;
        }
    }

    /**
     * Determines whether the given string appears to be a URI. A string is considered
     * to resemble a URI if it begins with a slash ("/"), "http://", or "https://".
     *
     * @param s the input string to evaluate; can be null
     *
     * @return true if the input string appears to be a URI, otherwise false
     */
    private boolean looksLikeUri(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        String t = s.trim();
        return t.startsWith("/") || t.startsWith("http://") || t.startsWith("https://");
    }

    /**
     * Determines whether the given class represents a primitive type or its corresponding wrapper type.
     *
     * @param c the {@code Class} to check; must not be null
     *
     * @return {@code true} if the class represents a primitive type or its wrapper (e.g., {@code int} or {@code
     * Integer}),
     * {@code false} otherwise
     */
    private boolean isPrimitiveOrWrapper(Class<?> c) {
        return c.isPrimitive() || c == Integer.class || c == Long.class || c == Double.class || c == Float.class || c == Boolean.class || c == Byte.class || c == Short.class || c == Character.class;
    }

    /**
     * Sanitizes the provided collection by iterating over its elements and replacing any values
     * that require sanitization. Elements in the collection are processed and sanitized according
     * to specific rules. If changes are made, the collection is updated with the sanitized values.
     * The method ensures that objects are not processed repeatedly by tracking visited objects.
     *
     * @param col     the collection to sanitize; must not be null
     * @param visited a set of already visited objects to prevent cyclic references during sanitization; must not be
     *                null
     */
    private void sanitizeCollection(Collection<?> col, Set<Object> visited) {
        List<Object> snapshot = new ArrayList<>(col);
        boolean changed = false;

        for (int i = 0; i < snapshot.size(); i++) {
            Object old = snapshot.get(i);
            Object replaced = sanitizeValue(old, visited);
            if (replaced != old) {
                snapshot.set(i, replaced);
                changed = true;
            }
        }

        if (changed) {
            try {
                col.clear();
                @SuppressWarnings("unchecked") Collection<Object> raw = (Collection<Object>) col;
                raw.addAll(snapshot);
            } catch (Exception e) {
                LOG.log(Level.FINE, "Collection unmodifiable", e);
            }
        }
    }

    /**
     * Recursively sanitizes a map by processing its keys and values according to specific sanitization rules.
     * This method ensures that the map is cleaned and updated with sanitized keys and values. If any changes
     * are made to a key or value, the old entry is removed and a new one with the sanitized data is added.
     * The method also tracks visited objects to avoid cyclical processing during the sanitization process.
     *
     * @param map     the map to sanitize, where keys and values are recursively processed; must not be null
     * @param visited a set of already visited objects to prevent cyclic references during sanitization;
     *                must not be null and should include objects being processed
     */
    private void sanitizeMap(Map<?, ?> map, Set<Object> visited) {
        List<Map.Entry<Object, Object>> toPut = new ArrayList<>();
        List<Object> toRemove = new ArrayList<>();

        for (Map.Entry<?, ?> e : map.entrySet()) {
            Object newKey = sanitizeValue(e.getKey(), visited);
            Object newVal = sanitizeValue(e.getValue(), visited);

            if (newKey != e.getKey() || newVal != e.getValue()) {
                toRemove.add(e.getKey());
                toPut.add(new AbstractMap.SimpleEntry<>(newKey, newVal));
            }
        }

        for (Object k : toRemove) {
            map.remove(k);
        }

        @SuppressWarnings("unchecked") Map<Object, Object> raw = (Map<Object, Object>) map;
        toPut.forEach(e -> raw.put(e.getKey(), e.getValue()));
    }

    /**
     * Recursively sanitizes the elements of an array. This method iterates over each
     * element of the provided array and applies sanitization rules to its contents.
     * If an element is sanitized and modified, the updated value is set back into the array.
     * Objects that have already been visited are skipped to prevent cyclical processing.
     *
     * @param array   the array to sanitize; must not be null
     * @param visited a set of already visited objects to prevent cyclic references during sanitization; must not be
     *                null
     */
    private void sanitizeArray(Object array, Set<Object> visited) {
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            Object old = Array.get(array, i);
            Object replaced = sanitizeValue(old, visited);
            if (replaced != old) {
                Array.set(array, i, replaced);
            }
        }
    }
}
