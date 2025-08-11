package org.app.common.eav.v2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UniversalManager {
    private static final Map<Class<?>, Map<String, Class<?>>> CLASS_CACHE = new ConcurrentHashMap<>();

    public static void registerDynamicClass(Class<?> baseClass, String id, Class<?> dynamicClass) {
        CLASS_CACHE
                .computeIfAbsent(baseClass, k -> new ConcurrentHashMap<>())
                .put(id, dynamicClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(Class<T> baseClass, String id) {
        String key = baseClass.getSimpleName() + "_" + id;
        var clazz = CLASS_CACHE.getOrDefault(baseClass, Map.of()).get(key);
        return (Class<T>) clazz;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassOrThrow(Class<T> baseClass, String id) {
        String key = baseClass.getSimpleName() + "_" + id;
        var map = CLASS_CACHE.get(baseClass);
        if (map == null) throw new IllegalArgumentException("No dynamic class found for " + baseClass.getName());
        var clazz = map.get(key);
        return (Class<T>) clazz;
    }

    public static <T> void onChange(Class<T> baseClass, String id, Iterable<EAVField> EAVs) {
        String key = baseClass.getSimpleName() + "_" + id;
        CLASS_CACHE.get(baseClass).remove(key);
        DynamicClassBuilder.of(baseClass, id, EAVs).compile();
    }
}
