package org.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.app.core.accessor.Invoker;
import org.app.core.accessor.PropertyAccessor;
import org.app.core.accessor.method.MethodAccessorCache;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small reflection helpers implemented using core Java (no MethodUtils/StringUtils required).
 * Methods are intentionally short, well-documented and provide sensible fallbacks
 * (e.g., direct field access if setter not found).
 */

// can extend to develop more methods
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ClassUtils {

    /**
     * Get set class impl by interface class
     *
     * @param clazz interface class
     */
    public static <T> Set<Class<? extends T>> getClasses(Class<T> clazz) {
        Reflections reflections = new Reflections(clazz.getPackageName());
        return reflections.getSubTypesOf(clazz);
    }

    public static String getClassName(Object obj) {
        return obj.getClass().getSimpleName();
    }

    @SneakyThrows
    public static Class<?> getClassByName(String className) {
        return Class.forName(className);
    }

    @SneakyThrows
    public static Class<?> getClassField(Object obj, String fieldName) {
        return obj.getClass().getDeclaredField(fieldName).getType();
    }

    // ------------------------- Field utilities -------------------------
    /**
     * Invoke a no-arg static method and cast the result to the target type.
     *
     * @param clazz      class that declares the static method
     * @param methodName method name with no arguments
     * @param target     return type to cast to
     * @param <T>        generic return type
     * @return cast result or null if method not found
     */
    public static <T> T invokeMethod(Class<?> clazz, String methodName, Class<T> target) {
        var val = Invoker.method(clazz, methodName);
        return target.cast(val);
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeConstructor(Class<T> clazz) {
        return (T) Invoker.constructor(clazz);
    }

    public static <T> T invokeStaticMethod(Class<?> clazz, String methodName, Class<T> target) {
        var val = Invoker.staticMethod(clazz, methodName);
        return target.cast(val);
    }

    // ------------------------- Additional convenient helpers -------------------------
    /**
     * Get the declared type of field (searches superclasses). Returns null if not found.
     */
    @SneakyThrows
    public static Class<?> getFieldType(Object target, String fieldName) {
        if (target == null || fieldName == null) return null;
        Field f = getFieldFromHierarchy(target.getClass(), fieldName);
        return f == null ? null : f.getType();
    }

    // ===================== PUBLIC API =====================

    public static Object get(Object target, String field) {
        if (target == null || field == null || field.isEmpty()) return null;

        PropertyAccessor acc = MethodAccessorCache.get(target.getClass(), field);
        return acc != null ? acc.get(target) : null;
    }

    public static <T> T get(Object target, String field, Class<T> type) {
        Object val = get(target, field);
        return val == null ? null : type.cast(val);
    }

    public static void set(Object target, String field, Object value) {
        if (target == null || field == null || field.isEmpty()) return;

        PropertyAccessor acc = MethodAccessorCache.get(target.getClass(), field);
        if (acc != null) {
            acc.set(target, value);
        }
    }

    public static boolean hasField(Class<?> clazz, String field) {
        return MethodAccessorCache.get(clazz, field) != null;
    }

    public static void preload(Class<?> clazz) {
        MethodAccessorCache.preload(clazz);
    }

    // ----------- Caching for field types (used by AccessorCache) -----------
    private static final ConcurrentHashMap<Class<?>, Map<String, Class<?>>> FIELD_TYPE_CACHE = new ConcurrentHashMap<>();

    public static Class<?> getFieldTypeFast(Class<?> clazz, String field) {
        return FIELD_TYPE_CACHE
            .computeIfAbsent(clazz, ClassUtils::buildFieldTypeMap)
            .get(field);
    }

    private static Map<String, Class<?>> buildFieldTypeMap(Class<?> clazz) {
        Map<String, Class<?>> map = new HashMap<>();

        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (var f : current.getDeclaredFields()) {
                map.putIfAbsent(f.getName(), f.getType());
            }
            current = current.getSuperclass();
        }

        return map;
    }

    // ------------------------- Internal helpers -------------------------

    private static Field getFieldFromHierarchy(Class<?> cls, String name) {
        Class<?> current = cls;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
