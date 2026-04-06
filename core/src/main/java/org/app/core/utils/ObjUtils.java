package org.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.app.core.support.Verify;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

// can extend to develop more methods
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ObjUtils {
    public static Object getValFromField(Object request, String fieldName) {
        try {
            Field field = request.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            var v = field.get(request);
            field.setAccessible(false);
            return v;
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

    public static <T> T nonNullElse(T obj, T defaultObj) {
        return (obj != null) ? obj : defaultObj;
    }

    public static <T> T nonNullElseGet(T obj, Supplier<T> defaultSupplier) {
        return (obj != null) ? obj : defaultSupplier.get();
    }

    @SuppressWarnings("unchecked")
    public static <T, R> Map<T, R> isMap(Object obj) {
        Verify.dataNonNull(obj);
        String errorMessage = String.format("Error when converting %s to Map", obj.getClass().getSimpleName());
        Verify.requireInstanceOf(obj, Map.class, errorMessage);
        return (Map<T, R>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> isList(Object obj) {
        Verify.dataNonNull(obj);
        String errorMessage = String.format("Error when converting %s to List", obj.getClass().getSimpleName());
        Verify.requireInstanceOf(obj, List.class, errorMessage);
        return (List<T>) obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> isSet(Object obj) {
        Verify.dataNonNull(obj);
        String errorMessage = String.format("Error when converting %s to Set", obj.getClass().getSimpleName());
        Verify.requireInstanceOf(obj, Set.class, errorMessage);
        return (Set<T>) obj;
    }
}
