package org.app.common.design.legacy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectHolder {
    private static final int MAP_SIZE = 8;
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>(MAP_SIZE);

    public Object getObject(String objectKey) {
        return OBJECT_MAP.get(objectKey);
    }

    public <T> T getObject(Class<T> clazz) {
        return clazz.cast(
                OBJECT_MAP.values()
                        .stream()
                        .filter(clazz::isInstance)
                        .findAny()
                        .orElseThrow(() -> new IllegalArgumentException("Can't find any object of class " + clazz.getName()))
        );
    }

    public Object setObject(String objectKey, Object object) {
        return OBJECT_MAP.put(objectKey, object);
    }
}
