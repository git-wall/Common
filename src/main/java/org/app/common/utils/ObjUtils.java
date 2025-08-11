package org.app.common.utils;

import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ObjUtils {

    public static Object getValFromField(Object request, String fieldName) {
        try {
            Field field = request.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(request);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access field: " + fieldName);
        }
    }

    @SuppressWarnings("unchecked")
    public static int compare(Object a, Object b) {
        if (a instanceof Comparable<?> && b instanceof Comparable<?>) {
            return ((Comparable<Object>) a).compareTo(b);
        }
        throw new IllegalArgumentException("Values are not comparable");
    }

    public static <T> T nonNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    public static <T> T nonNull(T obj) {
        return nonNull(obj, "Object cannot be null");
    }

    public static <T> T nonNullElse(T obj, T defaultObj) {
        return (obj != null) ? obj : defaultObj;
    }

    public static <T> T nonNullElseGet(T obj, Supplier<T> defaultSupplier) {
        return (obj != null) ? obj : defaultSupplier.get();
    }

    public static <T, R> Map<T, R> isMap(Object obj) {
        String errorMessage = String.format("Error when converting %s to Map", obj.getClass().getSimpleName());
        Assert.isInstanceOf(Map.class, obj, errorMessage);
        return convert(Map.class, obj);
    }

    public static <T> List<T> isList(Object obj) {
        String errorMessage = String.format("Error when converting %s to List", obj.getClass().getSimpleName());
        Assert.isInstanceOf(List.class, obj, errorMessage);
        return convert(List.class, obj);
    }

    public static <T> Set<T> isSet(Object obj) {
        String errorMessage = String.format("Error when converting %s to Set", obj.getClass().getSimpleName());
        Assert.isInstanceOf(Set.class, obj, errorMessage);
        return convert(Set.class, obj);
    }

    public static <T> T convert(Class<T> cls, Object obj) {
        return cls.cast(obj);
    }
}
