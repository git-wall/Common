package org.app.common.utils;

public class DataUtils {
    public DataUtils() {
    }

    public static <T> T getOrDefault(T data, T defaultValue) {
        if (data == null) {
            return defaultValue;
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
    public static <T> T autoCast(Object data) {
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
