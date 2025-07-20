package com.multicloud.commonlib.annotations.masksensitive;

import java.lang.reflect.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for safely building string representations of objects
 * with masking support for sensitive fields.
 */
public final class MaskedToStringBuilder {
    private static final Logger LOGGER = Logger.getLogger(MaskedToStringBuilder.class.getName());

    // Default configuration values
    private static final String DEFAULT_FULL_MASK = "****";
    private static final String ERROR_PLACEHOLDER = "<error>";
    private static final String ELLIPSIS_SEPARATOR = ", ...";
    private static final int DEFAULT_MAX_DEPTH = 20;
    private static final int DEFAULT_MAX_COLLECTION_SIZE = 100;
    private static final int DEFAULT_MAX_PARTIAL_MASK_LENGTH = 20;
    private static final int DEFAULT_MAX_STRING_LENGTH = 1000;
    private static final int DEFAULT_MIN_PARTIAL_MASK_LENGTH = 4;
    private static final int DEFAULT_VISIBLE_CHARS = 4;

    // Configuration fields
    private static final AtomicReference<String> fullMask = new AtomicReference<>(DEFAULT_FULL_MASK);
    private static final AtomicInteger maxDepth = new AtomicInteger(DEFAULT_MAX_DEPTH);
    private static final AtomicInteger maxCollectionSize = new AtomicInteger(DEFAULT_MAX_COLLECTION_SIZE);
    private static final AtomicInteger maxPartialMaskLength = new AtomicInteger(DEFAULT_MAX_PARTIAL_MASK_LENGTH);
    private static final AtomicInteger maxStringLength = new AtomicInteger(DEFAULT_MAX_STRING_LENGTH);
    private static final AtomicInteger minPartialMaskLength = new AtomicInteger(DEFAULT_MIN_PARTIAL_MASK_LENGTH);
    private static final AtomicInteger visibleCharCount = new AtomicInteger(DEFAULT_VISIBLE_CHARS);
    @Setter
    private static volatile boolean suppressFieldAccessLogging = false;

    // Sensitive field names (can be customized)
    private static final Set<String> SENSITIVE_FIELD_NAMES = Collections.synchronizedSet(new HashSet<>(Set.of(
            "password", "secret", "token", "pin", "creditcard", "ssn", "cvv"
    )));


    /**
     * Set keywords to identify sensitive field names automatically.
     *
     * @param keywords Set of field name substrings (e.g., "password", "token") to be masked.
     */
    public static void setSensitiveKeywords(Set<String> keywords) {
        synchronized (SENSITIVE_FIELD_NAMES) {
            SENSITIVE_FIELD_NAMES.clear();
            if (keywords != null) SENSITIVE_FIELD_NAMES.addAll(keywords);
        }
    }

    // Field cache
    private static final ClassValue<List<Field>> FIELD_CACHE = new ClassValue<>() {
        @Override
        protected List<Field> computeValue(@NotNull Class<?> type) {
            List<Field> fields = new ArrayList<>();
            Class<?> current = type;
            while (current != null && current != Object.class) {
                for (Field f : current.getDeclaredFields()) {
                    if (!f.isSynthetic() && !Modifier.isTransient(f.getModifiers())) {
                        fields.add(f);
                    }
                }
                current = current.getSuperclass();
            }
            return Collections.unmodifiableList(fields);
        }
    };

    private MaskedToStringBuilder() {}

    // Configuration methods...

    /**
     * Sets the maximum depth for object traversal to prevent infinite recursion.
     *
     * @param value maximum depth
     */
    public static void setMaxDepth(int value) {
        if (value < 1) throw new IllegalArgumentException("Max depth must be positive");
        maxDepth.set(value);
    }


    /**
     * Sets the number of characters to show before/after masking when using partial masking.
     *
     * @param value number of visible characters
     */
    public static void setVisibleCharCount(int value) {
        if (value < 0) throw new IllegalArgumentException("Visible char count must be >= 0");
        visibleCharCount.set(value);
    }

    /**
     * Sets the maximum number of items to process in a collection.
     *
     * @param value maximum collection size
     */
    public static void setMaxCollectionSize(int value) {
        if (value < 1) throw new IllegalArgumentException("Max collection size must be positive");
        maxCollectionSize.set(value);
    }

    /**
     * Sets the maximum number of mask characters to use in partial masking.
     *
     * @param value maximum number of asterisks
     */
    public static void setMaxPartialMaskLength(int value) {
        if (value < 1) throw new IllegalArgumentException("Max partial mask length must be positive");
        maxPartialMaskLength.set(value);
    }


    /**
     * Sets the maximum length of strings before truncating them with ellipsis.
     *
     * @param value maximum string length
     */
    public static void setMaxStringLength(int value) {
        if (value < 1) throw new IllegalArgumentException("Max string length must be positive");
        maxStringLength.set(value);
    }


    /**
     * Sets the minimum string length required to apply partial masking.
     *
     * @param value minimum string length
     */
    public static void setMinPartialMaskLength(int value) {
        if (value < 1) throw new IllegalArgumentException("Min partial mask length must be positive");
        if (value > maxPartialMaskLength.get())
            throw new IllegalArgumentException("Min partial mask length cannot exceed max");
        minPartialMaskLength.set(value);
    }

    /**
     * Sets the full mask string (e.g., "****") used when masking sensitive values fully.
     *
     * @param mask the masking string to use
     */
    public static void setFullMask(String mask) {
        fullMask.set(mask != null ? mask : DEFAULT_FULL_MASK);
    }

    /**
     * Resets all masking configuration options to their default values.
     */
    public static void resetToDefaults() {
        maxDepth.set(DEFAULT_MAX_DEPTH);
        maxCollectionSize.set(DEFAULT_MAX_COLLECTION_SIZE);
        maxPartialMaskLength.set(DEFAULT_MAX_PARTIAL_MASK_LENGTH);
        maxStringLength.set(DEFAULT_MAX_STRING_LENGTH);
        minPartialMaskLength.set(DEFAULT_MIN_PARTIAL_MASK_LENGTH);
        visibleCharCount.set(DEFAULT_VISIBLE_CHARS);
        fullMask.set(DEFAULT_FULL_MASK);
        suppressFieldAccessLogging = false;
    }

    // Main API

    /**
     * Builds a masked string representation of the given object using default depth.
     *
     * @param obj the object to stringify
     * @return masked string
     */
    public static String build(Object obj) {
        return build(obj, maxDepth.get());
    }


    /**
     * Builds a masked string representation of the given object up to the specified depth.
     *
     * @param obj the object to stringify
     * @param customDepth max recursion depth
     * @return masked string
     */
    public static String build(Object obj, int customDepth) {
        Objects.requireNonNull(obj, "Object to build cannot be null");
        try {
            return buildRecursive(obj, createIdentitySet(), Math.min(customDepth, maxDepth.get()));
        } catch (StackOverflowError e) {
            logError("Stack overflow while building toString", e);
            return "<stack overflow>";
        }
    }

    private static String buildRecursive(Object obj, Set<Object> visited, int depth) {
        if (obj == null) return "null";
        if (depth <= 0) return "<max depth reached>";
        if (isPrimitiveOrWrapper(obj)) return formatPrimitive(obj);

        switch (obj) {
            case Optional<?> opt -> {
                return opt.map(o -> "Optional[" + buildRecursive(o, visited, depth - 1) + "]").orElse("Optional.empty");
            }
            case OptionalInt opt -> {
                return opt.isPresent() ? "OptionalInt[" + opt.getAsInt() + "]" : "OptionalInt.empty";
            }
            case OptionalLong opt -> {
                return opt.isPresent() ? "OptionalLong[" + opt.getAsLong() + "]" : "OptionalLong.empty";
            }
            case OptionalDouble opt -> {
                return opt.isPresent() ? "OptionalDouble[" + opt.getAsDouble() + "]" : "OptionalDouble.empty";
            }
            case Enum<?> e -> {
                return e.name();
            }
            default -> {
                // fall through
            }
        }

        if (Proxy.isProxyClass(obj.getClass())) return "<proxy>";
        if (visited.contains(obj)) return "<cyclic reference @" + System.identityHashCode(obj) + ">";
        visited.add(obj);

        try {
            Class<?> clazz = obj.getClass();
            if (clazz.isRecord()) return handleRecord(obj, clazz, visited, depth);

            StringBuilder sb = new StringBuilder(clazz.getSimpleName()).append("{");
            boolean first = true;

            for (Field field : getCachedFields(clazz)) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (!first) sb.append(", ");
                first = false;

                sb.append(field.getName()).append("=");

                try {
                    Object value = field.get(obj);  // No setAccessible
                    appendFieldValue(sb, field, value, visited, depth - 1);
                } catch (IllegalAccessException e) {
                    logFieldAccessError("Cannot access field: " + clazz.getName() + "." + field.getName(), e);
                    sb.append("<inaccessible>");
                } catch (Exception e) {
                    logFieldAccessError("Error accessing field: " + clazz.getName() + "." + field.getName(), e);
                    sb.append(ERROR_PLACEHOLDER);
                }
            }

            return sb.append("}").toString();
        } finally {
            visited.remove(obj);
        }
    }

    private static String handleRecord(Object recordObj, Class<?> clazz, Set<Object> visited, int depth) {
        try {
            StringBuilder sb = new StringBuilder(clazz.getSimpleName()).append("[");
            boolean first = true;
            for (RecordComponent rc : clazz.getRecordComponents()) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(rc.getName()).append("=");
                sb.append(processRecordComponent(rc, clazz, recordObj, visited, depth));
            }
            return sb.append("]").toString();
        } catch (Exception e) {
            logError("Record parsing failed", e);
            return clazz.getSimpleName() + ERROR_PLACEHOLDER;
        }
    }

    private static String processRecordComponent(RecordComponent rc, Class<?> clazz, Object recordObj, Set<Object> visited, int depth) {
        try {
            Object value = rc.getAccessor().invoke(recordObj);
            MaskSensitive annotation = rc.getAccessor().getAnnotation(MaskSensitive.class);
            if (annotation == null) {
                Field field = getDeclaredFieldIgnoreErrors(clazz, rc.getName());
                if (field != null) {
                    annotation = field.getAnnotation(MaskSensitive.class);
                }
            }
            if (annotation == null && clazz.isAnnotationPresent(MaskSensitive.class)) {
                annotation = clazz.getAnnotation(MaskSensitive.class);
            }

            boolean partial = annotation != null && annotation.partial();
            boolean shouldMask = annotation != null || clazz.isAnnotationPresent(MaskSensitive.class) || isSensitiveFieldName(rc.getName());

            if (shouldMask) {
                return maskValue(value, partial);
            } else {
                StringBuilder sb = new StringBuilder();
                appendFieldValue(sb, null, value, visited, depth - 1);
                return sb.toString();
            }
        } catch (Exception e) {
            logFieldAccessError("Error accessing record component: " + rc.getName(), e);
            return ERROR_PLACEHOLDER;
        }
    }


    private static void appendFieldValue(StringBuilder sb, Field field, Object value, Set<Object> visited, int depth) {
        try {
            if (value == null) {
                sb.append("null");
            } else if (value instanceof char[]) {
                sb.append("\"").append(fullMask.get()).append("\"");
            } else if ((field != null && (field.isAnnotationPresent(MaskSensitive.class) || isSensitiveField(field)))
                    || (field == null && isSensitiveFieldName(safeToString(value)))) {
                boolean partial = field != null && field.isAnnotationPresent(MaskSensitive.class)
                        && field.getAnnotation(MaskSensitive.class).partial();
                sb.append(maskValue(value, partial));
            } else if (value instanceof Collection<?> col) {
                sb.append(handleCollection(col, visited, depth));
            } else if (value instanceof Map<?, ?> map) {
                sb.append(handleMap(map, visited, depth));
            } else if (value.getClass().isArray()) {
                sb.append(handleArray(value, visited, depth));
            } else if (value instanceof Class<?>) {
                sb.append("<class ").append(((Class<?>) value).getSimpleName()).append(">");
            } else {
                sb.append(buildRecursive(value, visited, depth));
            }
        } catch (Exception e) {
            logError("Error processing field value", e);
            sb.append(ERROR_PLACEHOLDER);
        }
    }

    private static String maskValue(Object value, boolean partial) {
        if (value == null) return "null";
        String str;
        try {
            str = value.toString();
        } catch (Exception e) {
            logError("Value toString() failed", e);
            return "\"" + fullMask.get() + "\"";
        }
        if (str.isEmpty()) return "\"\"";
        if (str.length() > maxStringLength.get()) {
            str = str.substring(0, maxStringLength.get()) + "...";
        }
        int cpCount = str.codePointCount(0, str.length());
        if (!partial || cpCount <= minPartialMaskLength.get()) return "\"" + fullMask.get() + "\"";

        int visible = Math.min(visibleCharCount.get(), cpCount / 2);
        try {
            int headEnd = str.offsetByCodePoints(0, visible);
            int tailStart = str.offsetByCodePoints(0, cpCount - visible);
            String head = str.substring(0, headEnd);
            String tail = str.substring(tailStart);
            int maskLen = Math.max(1, cpCount - visible * 2);
            return "\"" + head + "*".repeat(Math.min(maskLen, maxPartialMaskLength.get())) + tail + "\"";
        } catch (Exception e) {
            logError("Partial masking failed", e);
            return "\"" + fullMask.get() + "\"";
        }
    }

    private static String handleCollection(Collection<?> collection, Set<Object> visited, int depth) {
        if (collection == null) return "null";
        if (visited.contains(collection)) return "<cyclic collection>";
        if (collection.size() > maxCollectionSize.get()) return "<collection size=" + collection.size() + ">";
        visited.add(collection);
        try {
            StringBuilder sb = new StringBuilder("[");
            Iterator<?> it = collection.iterator();
            int count = 0;
            while (it.hasNext() && count < maxCollectionSize.get()) {
                if (count > 0) sb.append(", ");
                sb.append(buildRecursive(it.next(), visited, depth));
                count++;
                if (count >= maxCollectionSize.get() && it.hasNext()) {
                    sb.append(ELLIPSIS_SEPARATOR);
                    break;
                }
            }
            return sb.append("]").toString();
        } finally {
            visited.remove(collection);
        }
    }

    private static String handleMap(Map<?, ?> map, Set<Object> visited, int depth) {
        if (map == null) return "null";
        if (visited.contains(map)) return "<cyclic map>";
        if (map.size() > maxCollectionSize.get()) return "<map size=" + map.size() + ">";
        visited.add(map);
        try {
            StringBuilder sb = new StringBuilder("{");
            Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
            int count = 0;
            while (it.hasNext() && count < maxCollectionSize.get()) {
                if (count > 0) sb.append(", ");
                Map.Entry<?, ?> entry = it.next();
                sb.append(buildRecursive(entry.getKey(), visited, depth))
                        .append("=")
                        .append(buildRecursive(entry.getValue(), visited, depth));
                count++;
                if (count >= maxCollectionSize.get() && it.hasNext()) {
                    sb.append(ELLIPSIS_SEPARATOR);
                    break;
                }
            }
            return sb.append("}").toString();
        } finally {
            visited.remove(map);
        }
    }

    private static String handleArray(Object array, Set<Object> visited, int depth) {
        if (array == null) return "null";
        if (visited.contains(array)) return "<cyclic array>";
        int length = Array.getLength(array);
        if (length > maxCollectionSize.get()) return "<array length=" + length + ">";
        visited.add(array);
        try {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < Math.min(length, maxCollectionSize.get()); i++) {
                if (i > 0) sb.append(", ");
                sb.append(buildRecursive(Array.get(array, i), visited, depth));
                if (i == maxCollectionSize.get() - 1 && i < length - 1) {
                    sb.append(ELLIPSIS_SEPARATOR);
                    break;
                }
            }
            return sb.append("]").toString();
        } finally {
            visited.remove(array);
        }
    }

    private static String formatPrimitive(Object obj) {
        try {
            if (obj instanceof String str) return "\"" + str + "\"";
            if (obj instanceof Character ch) return "'" + ch + "'";
            if (obj instanceof byte[] bytes) return "<byte[" + bytes.length + "]>";
            if (obj instanceof short[] shorts) return "<short[" + shorts.length + "]>";
            if (obj instanceof int[] ints) return Arrays.toString(ints);
            if (obj instanceof long[] longs) return Arrays.toString(longs);
            if (obj instanceof float[] floats) return Arrays.toString(floats);
            if (obj instanceof double[] doubles) return Arrays.toString(doubles);
            if (obj instanceof char[] chars) return "<char[" + chars.length + "]>";
            if (obj instanceof boolean[] booleans) return Arrays.toString(booleans);
            if (obj instanceof Date date) return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
            if (obj instanceof java.time.temporal.TemporalAccessor temporal) return DateTimeFormatter.ISO_DATE_TIME.format(temporal);
            if (obj instanceof UUID uuid) return uuid.toString();
        } catch (Exception e) {
            logError("Primitive formatting failed", e);
            return ERROR_PLACEHOLDER;
        }
        return String.valueOf(obj);
    }

    private static boolean isSensitiveField(Field field) {
        return field != null && isSensitiveFieldName(field.getName());
    }

    private static boolean isSensitiveFieldName(String name) {
        if (name == null) return false;
        String lname = name.toLowerCase();
        return SENSITIVE_FIELD_NAMES.stream().anyMatch(lname::contains);
    }

    private static boolean isPrimitiveOrWrapper(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean ||
                obj instanceof Character || obj instanceof Date || obj instanceof UUID ||
                obj instanceof java.time.temporal.TemporalAccessor;
    }

    private static Set<Object> createIdentitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private static List<Field> getCachedFields(Class<?> type) {
        return FIELD_CACHE.get(type);
    }

    private static void logError(String message, Throwable t) {
        if (!suppressFieldAccessLogging) {
            LOGGER.log(Level.WARNING, message, t);
        }
    }

    private static void logFieldAccessError(String message, Throwable t) {
        if (!suppressFieldAccessLogging) {
            LOGGER.log(Level.WARNING, message, t);
        }
    }

    private static String safeToString(Object obj) {
        try {
            return obj != null ? obj.toString() : "null";
        } catch (Exception e) {
            logError("safeToString failed", e);
            return ERROR_PLACEHOLDER;
        }
    }

    private static Field getDeclaredFieldIgnoreErrors(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException e) {
            logError("Could not access field: " + clazz.getName() + "." + name, e);
            return null;
        }
    }
}