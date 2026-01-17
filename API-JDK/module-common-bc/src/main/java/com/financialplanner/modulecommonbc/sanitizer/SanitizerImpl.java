package com.financialplanner.modulecommonbc.sanitizer;

import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;
import com.financialplanner.modulecommonbc.sanitizer.annotations.StrictText;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Component
public class SanitizerImpl implements Sanitizer {

    private static final Logger LOG = Logger.getLogger(SanitizerImpl.class.getName());

    // ============================================================
    //  Layer A: Normalization
    // ============================================================

    private static String normalize(String s) {
        if (s == null) return null;

        return s.replace("\uFEFF", "")                             // BOM
                .replaceAll("[\\u200B-\\u200F\\u202A-\\u202E]", "") // zero-width/invisible
                .replaceAll("\\p{Cc}", "")                         // control chars
                .trim()
                .replaceAll("\\s{2,}", " ");                       // collapse whitespace
    }

    // ============================================================
    //  Layer B: Strict Validation
    // ============================================================

    private static final Pattern STRICT_TEXT =
        Pattern.compile("[\\p{L}0-9 \\-_'.]*");

    private static String sanitizeStrictText(String s) {
        s = normalize(s);
        if (s == null) return null;

        if (!STRICT_TEXT.matcher(s).matches()) {
            throw new IllegalArgumentException("Invalid characters in string");
        }
        return s;
    }

    // ============================================================
    //  Layer C: Lenient Text (free-form)
    // ============================================================

    private static String sanitizeLenientText(String s) {
        return normalize(s);
    }

    // ============================================================
    //  URI Sanitization (structured)
    // ============================================================

    private static final Pattern URI_PATTERN =
        Pattern.compile("[A-Za-z0-9:/?&=._#%\\-+]*");

    private static String sanitizeUri(String s) {
        s = normalize(s);
        if (s == null) return null;

        if (!URI_PATTERN.matcher(s).matches()) {
            throw new IllegalArgumentException("Invalid characters in URI string");
        }
        return s;
    }

    // ============================================================
    //  Public API
    // ============================================================

    @Override
    public void sanitize(Object root) {
        if (root == null) return;
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        sanitizeValue(root, visited);
    }

    @Override
    public String sanitizeString(String value) {
        return sanitizeStrictText(value);
    }

    // ============================================================
    //  Core Recursive Sanitization
    // ============================================================

    private Object sanitizeValue(Object value, Set<Object> visited) {
        if (value == null) return null;

        Class<?> clazz = value.getClass();

        if (isPrimitiveOrWrapper(clazz) || clazz.isEnum()) {
            return value;
        }

        if (visited.contains(value)) {
            return value;
        }
        visited.add(value);

        // --- String routing (default strict) ---
        if (value instanceof String s) {
            if (looksLikeUri(s)) {
                return sanitizeUri(s);
            }
            return sanitizeStrictText(s);
        }

        // --- Collections ---
        if (value instanceof Collection<?> col) {
            sanitizeCollection(col, visited);
            return value;
        }

        // --- Maps ---
        if (value instanceof Map<?, ?> map) {
            sanitizeMap(map, visited);
            return value;
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
    //  Annotation Routing for POJO Fields
    // ============================================================

    private void sanitizePojoFields(Object target, Class<?> clazz, Set<Object> visited) {
        for (Field field : getAllFields(clazz)) {

            int mods = field.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) continue;

            try {
                field.setAccessible(true);
                Object old = field.get(target);
                Object replaced;

                if (old instanceof String s) {

                    if (field.isAnnotationPresent(LenientText.class)) {
                        replaced = sanitizeLenientText(s);

                    } else if (field.isAnnotationPresent(StrictText.class)) {
                        replaced = sanitizeStrictText(s);

                    } else if (looksLikeUri(s)) {
                        replaced = sanitizeUri(s);

                    } else {
                        replaced = sanitizeStrictText(s);
                    }

                } else {
                    replaced = sanitizeValue(old, visited);
                }

                if (replaced != old) {
                    try {
                        field.set(target, replaced);
                    } catch (Exception e) {
                        LOG.log(Level.FINE, "Cannot set field " + field.getName(), e);
                    }
                }

            } catch (IllegalArgumentException iae) {
                throw iae;
            } catch (Exception e) {
                LOG.log(Level.FINE, "Error accessing field " + field.getName(), e);
            }
        }
    }

    // ============================================================
    //  Record Component Annotation Routing
    // ============================================================

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

                    } else if (rc.isAnnotationPresent(StrictText.class)) {
                        replaced = sanitizeStrictText(s);

                    } else if (looksLikeUri(s)) {
                        replaced = sanitizeUri(s);

                    } else {
                        replaced = sanitizeStrictText(s);
                    }

                } else {
                    replaced = sanitizeValue(old, visited);
                }

                args[i] = replaced;
                if (replaced != old) changed = true;
            }

            if (!changed) return record;

            Constructor<?> ctor = clazz.getDeclaredConstructor(
                Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new)
                                                              );
            ctor.setAccessible(true);
            return ctor.newInstance(args);

        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to sanitize record " + clazz.getName(), e);
            return null;
        }
    }

    // ============================================================
    //  Helpers
    // ============================================================

    private boolean looksLikeUri(String s) {
        if (s == null || s.isEmpty()) return false;
        String t = s.trim();
        return t.startsWith("/") || t.startsWith("http://") || t.startsWith("https://");
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private boolean isPrimitiveOrWrapper(Class<?> c) {
        return c.isPrimitive() ||
               c == Integer.class || c == Long.class || c == Double.class ||
               c == Float.class || c == Boolean.class || c == Byte.class ||
               c == Short.class || c == Character.class;
    }

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
                @SuppressWarnings("unchecked")
                Collection<Object> raw = (Collection<Object>) col;
                raw.addAll(snapshot);
            } catch (Exception e) {
                LOG.log(Level.FINE, "Collection unmodifiable", e);
            }
        }
    }

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

        for (Object k : toRemove) map.remove(k);

        @SuppressWarnings("unchecked")
        Map<Object, Object> raw = (Map<Object, Object>) map;
        toPut.forEach(e -> raw.put(e.getKey(), e.getValue()));
    }

    private void sanitizeArray(Object array, Set<Object> visited) {
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            Object old = Array.get(array, i);
            Object replaced = sanitizeValue(old, visited);
            if (replaced != old) Array.set(array, i, replaced);
        }
    }
}
