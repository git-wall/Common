package org.app.common.utils;

import org.apache.commons.collections.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

public class DataUtils {
    private DataUtils() {
    }

    public static int of(Integer i) {
        return i == null ? 0 : i;
    }

    public static long of(Long l) {
        return l == null ? 0 : l;
    }

    public static double of(Double d) {
        return d == null ? 0.d : d;
    }

    public static float of(Float f) {
        return f == null ? 0.f : f;
    }

    public static String of(String s) {
        return s == null ? "" : s;
    }

    public static <T> T cast(Object o, Class<T> clazz) {
        try {
            return clazz.cast(o);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> castCollection(Object o, Class<T> clazz) {
        try {
            Collection<?> raw = (Collection) o;
            List<T> result = new ArrayList<>(raw.size());
            for (Object item : raw) {
                result.add(clazz.cast(item));
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T parse(Map<String, Object> map, String key, Class<T> clazz) {
        Object val = map.getOrDefault(key, null);

        if (val == null) {
            return null;
        }

        if (clazz.isInstance(val)) {
            return clazz.cast(val);
        }

        if (clazz == Long.class && val instanceof String) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(val.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                Long epochMilli = ldt.toInstant(ZoneOffset.UTC).toEpochMilli();
                return clazz.cast(epochMilli);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    public static <T> T parseOrDefault(Map<String, Object> map, String key, Class<T> clazz, T defaultValue) {
        var val = map.getOrDefault(key, null);

        if (val == null) {
            return defaultValue;
        }

        if (clazz.isInstance(val)) {
            return clazz.cast(val);
        }

        if (clazz == Long.class && val instanceof String) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(val.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                Long epochMilli = ldt.toInstant(ZoneOffset.UTC).toEpochMilli();
                return clazz.cast(epochMilli);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    public static <T> T nullable(Supplier<T> supplier) {
        return supplier == null ? null : supplier.get();
    }

    public static <T> T ifNullDefault(T data, T defaultValue) {
        if (data == null) {
            return defaultValue;
        }
        return data;
    }

    public static <T> List<T> ifEmptyDefault(List<T> data, List<T> defaultValue) {
        if (CollectionUtils.isEmpty(data)) {
            return defaultValue;
        }
        return data;
    }

    public static <T> List<T> defaultListOf(List<T> data) {
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        return data;
    }

    public static <T> T safeType(T data, Class<T> type) {
        if (data == null) {
            return null;
        }

        if (type.isInstance(data)) {
            return type.cast(data);
        }

        throw new IllegalArgumentException("Data is not of type " + type.getName());
    }

    public static <T> T safeTypeOrDefault(T data, Class<T> type, T defaultValue) {
        if (data == null) {
            return defaultValue;
        }

        if (type.isInstance(data)) {
            return type.cast(data);
        }

        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T as(Object data) {
        if (data == null) {
            return null;
        }

        try {
            if (data instanceof String) {
                return (T) data.toString();
            } else if (data instanceof Integer) {
                return (T) Integer.valueOf(data.toString());
            } else if (data instanceof Long) {
                return (T) Long.valueOf(data.toString());
            } else if (data instanceof Double) {
                return (T) Double.valueOf(data.toString());
            } else if (data instanceof Float) {
                return (T) Float.valueOf(data.toString());
            } else if (data instanceof Boolean) {
                return (T) Boolean.valueOf(data.toString());
            } else {
                return (T) data; // Fallback to the original type
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Data cannot be cast to the expected type", e);
        }
    }
}
