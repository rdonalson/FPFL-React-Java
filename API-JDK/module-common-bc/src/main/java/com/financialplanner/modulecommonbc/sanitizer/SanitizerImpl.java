package com.financialplanner.modulecommonbc.sanitizer;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The `SanitizerImpl` class provides functionality for sanitizing various types of objects,
 * including primitive types, collections, arrays, maps, POJOs, and records. It ensures that
 * all sanitized data is safe to use in downstream operations by removing or processing
 * potentially harmful content. This class is designed to handle cyclic references gracefully
 * by tracking visited objects.
 * ---
 * The sanitization process supports the following:
 * - Normalizing and escaping input strings to remove harmful or invalid content.
 * - Recursively processing composite objects like collections, maps, and arrays.
 * - Escaping and validating URIs to ensure their safety.
 * - Ensuring data integrity for POJOs and records through field-based sanitization.
 * ---
 * Fields:
 * - LOG: A logger used for logging information or debugging messages during sanitization.
 * ---
 * Methods:
 * - `getAllFields`: Retrieves all fields of a class, including those from its superclasses.
 * - `sanitize`: Sanitizes a root object and all nested objects.
 * - `sanitizeValue`: Processes and sanitizes individual objects based on their type.
 * - `looksLikeUri`: Checks if a string appears to be a URI.
 * - `sanitizeCollection`: Sanitizes elements of a generic collection.
 * - `sanitizeList`: Sanitizes elements of a list.
 * - `sanitizeMap`: Sanitizes keys and values of a map.
 * - `sanitizeArray`: Sanitizes all elements in an array.
 * - `sanitizeRecord`: Sanitizes the components of a record.
 * - `sanitizePojoFields`: Traverses and sanitizes fields of a POJO.
 * - `isPrimitiveOrWrapper`: Checks if a class is a primitive type or its wrapper.
 * - `escape`: Sanitizes a string to remove unsafe or unwanted content.
 * - `escapeForUri`: Escapes and normalizes a string to ensure it forms a valid URI.
 */
@Component
public class SanitizerImpl implements Sanitizer {
    /**
     * Logger instance used for capturing and logging messages within the
     * {@code SanitizerImpl} class. It provides a centralized mechanism for
     * logging diagnostic and informative messages, including warnings and errors.
     * This logger is utilized throughout the class to ensure consistent and
     * structured logging.
     * ---
     * The logger is configured with the fully qualified name of the containing
     * class, which allows for fine-grained control and filtering of log messages
     * based on package and class hierarchy. This is particularly useful in larger
     * applications for tracing and debugging operations.
     * ---
     * Being a {@code private static final} field, this logger:
     * - Is exclusive to the {@code SanitizerImpl} class.
     * - Facilitates efficient and consistent logging without needing re-initialization.
     * - Cannot be modified, ensuring thread safety in multi-threaded environments.
     * ---
     * Typical use cases include logging exceptions, warnings, and other significant
     * events that occur during the invocation of methods in this class.
     */
    private static final Logger LOG = Logger.getLogger(SanitizerImpl.class.getName());

    /**
     * Retrieves all fields of the specified class, including fields declared
     * in its superclasses, up to but not including the {@code Object} class.
     * ---
     *
     * @param type the class whose fields are to be retrieved; must not be {@code null}.
     * @return a list of {@link Field} objects representing all fields declared
     * in the given class and its superclasses.
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
     * Sanitizes the given root object by escaping and normalizing its values as needed.
     * Supports primitive types, collections, maps, arrays, records, and POJOs, ensuring that
     * cyclic references are handled gracefully.
     * ---
     *
     * @param root the root object to sanitize. If null, this method does nothing.
     */
    @Override
    public void sanitize(Object root) {
        if (root == null) {
            return;
        }
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        sanitizeValue(root, visited);
    }

    /**
     * Sanitizes the given value by processing it based on its type and content.
     * This method handles primitive types, enums, strings, collections, maps,
     * arrays, records, and POJOs to ensure they are sanitized appropriately.
     * It also prevents cyclic processing by keeping track of visited objects.
     * ---
     *
     * @param value   The object that needs to be sanitized. It can be of any type
     *                including primitives, strings, collections, maps, arrays, or custom objects.
     * @param visited A set of objects that have been visited during the sanitization
     *                process to prevent processing the same object multiple times.
     * @return The sanitized object. Depending on the type, the original object
     * might be returned as-is or replaced with a sanitized version.
     */
    private Object sanitizeValue(Object value, Set<Object> visited) {
        if (value == null) {
            return null;
        }

        Class<?> clazz = value.getClass();

        // primitives, wrappers, enums -> nothing to do
        if (isPrimitiveOrWrapper(clazz) || clazz.isEnum()) {
            return value;
        }

        // avoid cycles (identity-based)
        if (visited.contains(value)) {
            return value;
        }
        visited.add(value);

        // Strings -> sanitize and return possibly new string
        switch (value) {

            // Heuristic: treat path-like or URL-like strings as URIs
            case String s -> {
                if (looksLikeUri(s)) {
                    return escapeUri(s);
                }
                else {
                    return escape(s);
                }
            }

            // Collections
            case Collection<?> collection -> {
                sanitizeCollection(collection, visited);
                return value;
            }

            // Maps
            case Map<?, ?> map -> {
                sanitizeMap(map, visited);
                return value;
            }
            default -> {
            }
        }

        // Arrays
        if (clazz.isArray()) {
            sanitizeArray(value, visited);
            return value;
        }

        // Records
        if (clazz.isRecord()) {
            Object newRecord = sanitizeRecord(value, clazz, visited);
            return newRecord != null ? newRecord : value;
        }

        // POJO fields (including inherited)
        sanitizePojoFields(value, clazz, visited);
        return value;
    }

    /**
     * Determines if the given string appears to be a URI.
     * A string is considered to look like a URI if it starts with "/", "http://", or "https://".
     * ---
     *
     * @param s the string to evaluate; can be null or empty
     * @return true if the string looks like a URI, false otherwise
     */
    private boolean looksLikeUri(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        String trimmed = s.trim();
        return trimmed.startsWith("/") || trimmed.startsWith("http://") || trimmed.startsWith("https://");
    }

    /**
     * Sanitizes the elements of the provided collection by replacing or modifying its elements
     * when necessary. If the collection is a {@link List}, it uses a specialized method for
     * list sanitization. For other collection types, it rebuilds the collection when changes
     * are detected, ensuring the original collection is updated with sanitized elements.
     * ---
     *
     * @param collection the collection whose elements are to be sanitized
     * @param visited    a set used to track visited objects and avoid cyclic processing
     */
    private void sanitizeCollection(Collection<?> collection, Set<Object> visited) {
        if (collection instanceof List<?> list) {
            sanitizeList(list, visited);
            return;
        }

        // For other collections, rebuild only if any element changes
        List<Object> snapshot = new ArrayList<>(collection);
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
                // clear and re-add via raw-type cast to avoid wildcard capture compile error
                collection.clear();
                @SuppressWarnings("unchecked") Collection<Object> raw = (Collection<Object>) collection;
                raw.addAll(snapshot);
            } catch (UnsupportedOperationException e) {
                LOG.log(Level.FINE, "Collection is unmodifiable, cannot replace elements", e);
            }
        }
    }

    /**
     * Sanitizes all elements in the specified list. Each element is processed to remove or replace
     * potentially harmful content, ensuring the list's elements are safe for use in downstream operations.
     * If the list is unmodifiable or in-place mutation is unsupported, a fallback mechanism creates
     * a sanitized copy and attempts to replace the original content.
     * ---
     *
     * @param list    The list to be sanitized. Null values in the list are ignored, and elements
     *                are replaced with sanitized versions when necessary.
     * @param visited A set used to track already visited objects in order to avoid infinite
     *                recursion or cyclic references during the sanitization process.
     */
    private void sanitizeList(List<?> list, Set<Object> visited) {
        // First attempt: mutate in-place using a ListIterator
        ListIterator<?> it = list.listIterator();
        try {
            while (it.hasNext()) {
                Object old = it.next();
                Object replaced = sanitizeValue(old, visited);
                if (replaced != old) {
                    @SuppressWarnings("unchecked") ListIterator<Object> rawIt = (ListIterator<Object>) it;
                    rawIt.set(replaced);
                }
            }
            return;
        } catch (UnsupportedOperationException |
                 IllegalStateException |
                 ClassCastException e) {
            // Fall through to rebuild approach
            LOG.log(Level.FINE, "In-place list mutation failed, will attempt rebuild", e);
        }

        // Fallback: build a snapshot with replacements
        List<Object> snapshot = new ArrayList<>(list.size());
        boolean changed = false;
        for (Object old : list) {
            Object replaced = sanitizeValue(old, visited);
            snapshot.add(replaced);
            if (replaced != old) {
                changed = true;
            }
        }

        if (!changed) {
            return; // nothing to do
        }

        try {
            list.clear();
            @SuppressWarnings("unchecked") List<Object> rawList = (List<Object>) list;
            rawList.addAll(snapshot);
        } catch (UnsupportedOperationException e) {
            LOG.log(Level.FINE, "List is unmodifiable, cannot replace elements", e);
        }
    }

    /**
     * Sanitizes the keys and values of a given map by recursively applying the sanitization
     * logic to its entries. If keys or values are modified during the sanitization process,
     * updates the map by replacing old entries with sanitized ones.
     * ---
     *
     * @param map     the map whose entries will be sanitized
     * @param visited a set to track visited objects and prevent infinite loops while
     *                processing cyclic references
     */
    private void sanitizeMap(Map<?, ?> map, Set<Object> visited) {
        // Use iterator to avoid ConcurrentModificationException and collect replacements
        List<Map.Entry<Object, Object>> toPut = new ArrayList<>();
        List<Object> toRemove = new ArrayList<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object rawKey = entry.getKey();
            Object rawVal = entry.getValue();

            Object newKey = sanitizeValue(rawKey, visited);
            Object newVal = sanitizeValue(rawVal, visited);

            boolean keyChanged = newKey != rawKey;
            boolean valChanged = newVal != rawVal;

            if (keyChanged || valChanged) {
                toRemove.add(rawKey);
                toPut.add(new AbstractMap.SimpleEntry<>(newKey, newVal));
            }
        }

        // Apply removals and insert replacements
        for (Object k : toRemove) {
            map.remove(k);
        }

        @SuppressWarnings("unchecked") Map<Object, Object> rawMap = (Map<Object, Object>) map;
        toPut.forEach(e -> {
            if (e.getKey() != null) {
                rawMap.put(e.getKey(), e.getValue());
            }
        });
    }

    /**
     * Sanitizes an array by iterating through its elements and replacing them
     * with sanitized values if necessary. Each element is processed using the
     * specified sanitization logic and replaces the original element in the array
     * if modifications occur.
     * ---
     *
     * @param array   The array to be sanitized. Must not be null.
     * @param visited A set of already visited objects used to prevent cyclic references
     *                during sanitization. Must not be null.
     */
    private void sanitizeArray(Object array, Set<Object> visited) {
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            Object elem = Array.get(array, i);
            Object replaced = sanitizeValue(elem, visited);
            if (replaced != elem) {
                Array.set(array, i, replaced);
            }
        }
    }

    /**
     * Sanitizes a given record by processing its components and ensuring all values are safe.
     * If any value is modified during sanitization, a new record instance is created and returned.
     * Otherwise, the original record is returned.
     * ---
     * @param record  the record instance to sanitize; must not be null
     * @param clazz   the class of the record being sanitized; must not be null and must represent a record type
     * @param visited a set of objects already visited during sanitization, used to prevent cyclic processing; must
     *                not be null
     * @return the sanitized record instance, or null if sanitization fails
     * @throws IllegalArgumentException if any value in the record is deemed invalid during sanitization
     */
    private Object sanitizeRecord(Object record, Class<?> clazz, Set<Object> visited) {
        try {
            RecordComponent[] components = clazz.getRecordComponents();
            Object[] args = new Object[components.length];
            boolean changed = false;

            for (int i = 0; i < components.length; i++) {
                Method accessor = components[i].getAccessor();
                Object val = accessor.invoke(record);
                Object sanitized = sanitizeValue(val, visited);
                args[i] = sanitized;
                if (sanitized != val) {
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
        } catch (IllegalArgumentException iae) {
            // validation error from sanitizeValue/escape -> rethrow
            throw iae;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to sanitize record " + clazz.getName(), e);
            return null;
        }

    }

    /**
     * Sanitizes the fields of a plain old Java object (POJO) by traversing its declared fields,
     * processing non-static and non-transient fields recursively. Fields are sanitized
     * by applying value-specific sanitization rules or escaping potentially unsafe content.
     * ---
     *
     * @param target  The object whose fields need to be sanitized. This can be any POJO
     *                requiring sanitization.
     * @param clazz   The class of the object to sanitize. This allows accessing fields,
     *                including inherited fields from superclasses.
     * @param visited A set of objects already visited during the sanitization process,
     *                to prevent infinite cycles when the object graph contains references
     *                to objects in a loop.
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
                Object replaced = sanitizeValue(old, visited);

                if (replaced != old) {
                    try {
                        field.set(target, replaced);
                    } catch (IllegalAccessException |
                             IllegalArgumentException iae) {
                        // final fields or type mismatch; log and continue
                        LOG.log(Level.FINE, "Cannot set field " + field.getName() + " on " + clazz.getName(), iae);
                    }
                }
            } catch (IllegalArgumentException iae) {
                // validation error from sanitizeValue/escape -> rethrow so controller can handle it
                throw iae;
            } catch (Exception e) {
                // reflection/access errors only
                LOG.log(Level.FINE, "Error accessing field " + field.getName() + " on " + clazz.getName(), e);
            }
        }

    }

    /**
     * Determines if the specified class represents either a primitive type
     * or its corresponding wrapper type.
     * ---
     *
     * @param clazz the class to be checked; must not be null
     * @return true if the specified class is a primitive type or its corresponding
     * wrapper type, otherwise false
     */
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == Integer.class || clazz == Long.class || clazz == Double.class || clazz == Float.class || clazz == Boolean.class || clazz == Byte.class || clazz == Short.class || clazz == Character.class;
    }

    /**
     * Sanitizes the given input string by removing or normalizing potentially unsafe or unwanted content.
     * This includes removing control characters, invisible characters, HTML script tags, JavaScript URLs,
     * and event handlers, as well as collapsing whitespace and ensuring the string matches a whitelist of valid
     * characters.
     * ---
     *
     * @param input the input string to be sanitized; may be null
     * @return the sanitized string, or null if the input was null
     * @throws IllegalArgumentException if the input string contains invalid characters outside the allowed whitelist
     */
    private String escape(String input) {
        if (input == null) {
            return null;
        }

        // 1. Normalize Unicode
        String s = Normalizer.normalize(input, Normalizer.Form.NFC);

        // 2. Remove BOM + zero-width/invisible chars
        s = s.replace("\uFEFF", "")
             .replaceAll("[\\u200B-\\u200F\\u202A-\\u202E]", "");

        // 3. Remove all control characters
        s = s.replaceAll("\\p{Cc}", "");

        // 4. Trim + collapse whitespace
        s = s.trim()
             .replaceAll("\\s{2,}", " ");

        // 5. Strip script tags
        s = s.replaceAll("(?i)<script.*?>.*?</script>", "");

        // 6. Strip javascript: URLs
        s = s.replaceAll("(?i)javascript:", "");

        // 7. Strip event handlers (onclick=, onload=, etc.)
        s = s.replaceAll("(?i)on\\w+\\s*=", "");

        // 8. Optional whitelist validation
        if (!s.matches("[\\p{L}0-9 \\-_'.]*")) {
            throw new IllegalArgumentException("Invalid characters in string");
        }

        // 9, Return sanitized string
        return s;
    }

    /**
     * Escapes and normalizes a given string to ensure it is a valid URI. The method performs
     * Unicode normalization, removes invisible characters, control characters, and collapses
     * whitespace. If the input string is not a valid URI, an {@code IllegalArgumentException}
     * is thrown.
     * ---
     *
     * @param input the input string to escape and validate as a URI. If null, the method returns null.
     * @return a normalized and escaped string representing a valid URI, or null if the input is null.
     * @throws IllegalArgumentException if the input string is not a valid URI.
     */
    private String escapeUri(String input) {
        if (input == null) {
            return null;
        }

        // Basic normalization steps similar to escape()
        String s = Normalizer.normalize(input, Normalizer.Form.NFC);
        s = s.replace("\uFEFF", "")
             .replaceAll("[\\u200B-\\u200F\\u202A-\\u202E]", "")
             .replaceAll("\\p{Cc}", "")
             .trim()
             .replaceAll("\\s{2,}", " ");

        // Validate as URI
        try {
            // Use URI to parse and normalize; this will throw URISyntaxException for invalid URIs
            URI uri = new URI(s).normalize();
            return StringEscapeUtils.escapeHtml4(uri.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + input, e);
        }
    }
}
