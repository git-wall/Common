package org.app.core.accessor.lambda;

import org.app.core.accessor.PropertyAccessor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class LambdaAccessorCache {

    private static final ConcurrentHashMap<Class<?>, Map<String, PropertyAccessor>> CACHE = new ConcurrentHashMap<>();

    public static PropertyAccessor get(Class<?> clazz, String field) {
        return CACHE
            .computeIfAbsent(clazz, LambdaAccessorCache::build)
            .get(field);
    }

    private static Map<String, PropertyAccessor> build(Class<?> clazz) {
        Map<String, Method> getters = new HashMap<>();
        Map<String, Method> setters = new HashMap<>();

        for (Method m : clazz.getMethods()) {
            if (isGetter(m)) {
                getters.put(extractField(m.getName()), m);
            } else if (isSetter(m)) {
                setters.put(extractField(m.getName()), m);
            }
        }

        Map<String, PropertyAccessor> result = new HashMap<>();

        Set<String> fields = new HashSet<>();
        fields.addAll(getters.keySet());
        fields.addAll(setters.keySet());

        for (String field : fields) {
            Function<Object, Object> getter = null;
            BiConsumer<Object, Object> setter = null;

            Method gm = getters.get(field);
            Method sm = setters.get(field);

            if (gm != null) {
                getter = LambdaFactory.createGetter(gm);
            }

            if (sm != null) {
                setter = LambdaFactory.createSetter(sm);
            }

            result.put(field, new LambdaAccessor(getter, setter));
        }

        return result;
    }

    private static boolean isGetter(Method m) {
        return m.getParameterCount() == 0
            && !void.class.equals(m.getReturnType())
            && (m.getName().startsWith("get") || m.getName().startsWith("is"));
    }

    private static boolean isSetter(Method m) {
        return m.getParameterCount() == 1
            && m.getName().startsWith("set");
    }

    private static String extractField(String methodName) {
        String name = methodName.startsWith("is")
            ? methodName.substring(2)
            : methodName.substring(3);

        if (name.isEmpty()) return name;

        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
