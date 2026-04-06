package org.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    // ------------------------- Core-Java getters/setters -------------------------

    /**
     * Invoke a getter method for the given field name on the target object.
     * Tries both "getXxx" and "isXxx" forms, and falls back to direct field access
     * if no getter is found.
     *
     * @param target object to read from
     * @param field  plain field name (e.g. "name")
     * @return getter value or null if not found
     */
    @SneakyThrows
    public static Object invokeGetMethod(Object target, String field) {
        if (target == null || field == null || field.isEmpty()) return null;
        Class<?> cls = target.getClass();

        String capitalized = capitalize(field);
        String[] getterNames = {"get" + capitalized, "is" + capitalized};

        for (String name : getterNames) {
            try {
                Method m = cls.getMethod(name);
                m.setAccessible(true);
                return m.invoke(target);
            } catch (NoSuchMethodException ignored) {
                // try next
            }
        }

        // fallback to direct field access
        Field f = getFieldFromHierarchy(cls, field);
        if (f != null) {
            f.setAccessible(true);
            return f.get(target);
        }
        return null;
    }

    /**
     * Invoke a getter using a Field reference (delegates to name-based getter).
     */
    @SneakyThrows
    public static Object invokeGetMethod(Object target, Field field) {
        if (field == null) return null;
        return invokeGetMethod(target, field.getName());
    }

    /**
     * Invoke setter if present (tries matching setter method by name and single parameter),
     * otherwise tries to set the declared field directly.
     *
     * @param target     object to modify
     * @param field      plain field name
     * @param fieldValue value to set (maybe null)
     */
    @SneakyThrows
    public static void invokeSetMethod(Object target, String field, Object fieldValue) {
        if (target == null || field == null || field.isEmpty()) return;
        Class<?> cls = target.getClass();
        String setterName = "set" + capitalize(field);

        // try to find a setter with single parameter (match by name only, accept assignable types)
        for (Method m : cls.getMethods()) {
            if (!m.getName().equals(setterName)) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1) continue;
            // null value: accept first setter
            if (fieldValue == null || params[0].isAssignableFrom(fieldValue.getClass())) {
                m.setAccessible(true);
                m.invoke(target, fieldValue);
                return;
            }
        }

        // fallback to direct field set
        Field f = getFieldFromHierarchy(cls, field);
        if (f != null) {
            f.setAccessible(true);
            f.set(target, fieldValue);
        }
    }

    /**
     * Invoke setter using a Field reference (delegates to name-based setter and fallback to direct field set).
     */
    @SneakyThrows
    public static void invokeSetMethod(Object target, Field field, Object fieldValue) {
        if (field == null) return;
        invokeSetMethod(target, field.getName(), fieldValue);
    }

    // ------------------------- Field utilities -------------------------

    /**
     * Return a list of declared field names for a class.
     */
    public static List<String> getClassFieldNames(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        return java.util.Arrays.stream(fields)
            .map(Field::getName)
            .collect(Collectors.toList());
    }

    /**
     * Invoke a no-arg static method and cast the result to the target type.
     *
     * @param clazz      class that declares the static method
     * @param methodName method name with no arguments
     * @param target     return type to cast to
     * @param <T>        generic return type
     * @return casted result or null if method not found
     */
    @SneakyThrows
    public static <T> T invokeMethod(Class<?> clazz, String methodName, Class<T> target) {
        Method method = clazz.getMethod(methodName);
        method.setAccessible(true);
        Object o = method.invoke(null);
        return target.cast(o);
    }

    // ------------------------- Additional convenient helpers -------------------------

    /**
     * Read a field value by name using reflection. This searches superclasses as well.
     */
    @SneakyThrows
    public static Object getFieldValue(Object target, String fieldName) {
        if (target == null || fieldName == null) return null;
        Field f = getFieldFromHierarchy(target.getClass(), fieldName);
        if (f == null) return null;
        f.setAccessible(true);
        return f.get(target);
    }

    /**
     * Set a field value by name using reflection. Searches superclasses as well.
     */
    @SneakyThrows
    public static void setFieldValue(Object target, String fieldName, Object value) {
        if (target == null || fieldName == null) return;
        Field f = getFieldFromHierarchy(target.getClass(), fieldName);
        if (f == null) return;
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * Get the declared type of field (searches superclasses). Returns null if not found.
     */
    @SneakyThrows
    public static Class<?> getFieldType(Object target, String fieldName) {
        if (target == null || fieldName == null) return null;
        Field f = getFieldFromHierarchy(target.getClass(), fieldName);
        return f == null ? null : f.getType();
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
