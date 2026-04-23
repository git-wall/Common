package org.app.core.accessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Invoker {
    private static final Map<String, MethodHandle> CACHE = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final String FORMAT = "%s.%s";

    public static Object method(Class<?> clazz, String methodName) {
        return getObject(clazz, methodName, FuncType.METHOD);
    }

    public static Object staticMethod(Class<?> clazz, String methodName) {
        return getObject(clazz, methodName, FuncType.STATIC);
    }

    public static Object constructor(Class<?> clazz) {
        return getObject(clazz, null, FuncType.CONSTRUCTOR);
    }

    public static void setter(Class<?> clazz, String fieldName) {
        getObject(clazz, fieldName, FuncType.SETTER);
    }

    public static Object getter(Class<?> clazz, String fieldName) {
        return getObject(clazz, fieldName, FuncType.GETTER);
    }

    private static Object getObject(Class<?> clazz, String methodName, FuncType funcType) {
        String key = String.format(FORMAT, clazz.getName(), methodName);

        try {
            MethodHandle handle = CACHE.computeIfAbsent(key, k -> getMethodHandle(clazz, methodName, funcType));
            return handle.invoke();
        } catch (Throwable e) {
            throw new RuntimeException("Error call method", e);
        }
    }

    private static MethodHandle getMethodHandle(Class<?> clazz, String methodName, FuncType funcType) {
        try {
            MethodType type = MethodType.methodType(Object.class);
            switch (funcType) {
                case SETTER:
                    return LOOKUP.findSetter(clazz, methodName, Object.class);
                case GETTER:
                    return LOOKUP.findGetter(clazz, methodName, Object.class);
                case STATIC:
                    return LOOKUP.findStatic(clazz, methodName, type);
                case CONSTRUCTOR:
                    return LOOKUP.findConstructor(clazz, type);
                case METHOD:
                    default:
                    return LOOKUP.findVirtual(clazz, methodName, type);
            }
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Not found method: " + methodName, e);
        }
    }

    public enum FuncType {
        METHOD,
        STATIC,
        SETTER,
        GETTER,
        CONSTRUCTOR
    }
}
