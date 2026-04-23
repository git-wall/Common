package org.app.core.accessor.method;

import org.app.core.accessor.PropertyAccessor;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodAccessorCache {

    private static final ConcurrentHashMap<Class<?>, Map<String, PropertyAccessor>> CACHE =
        new ConcurrentHashMap<>();

    public static PropertyAccessor get(Class<?> clazz, String field) {
        Map<String, PropertyAccessor> map =
            CACHE.computeIfAbsent(clazz, MethodAccessorCache::build);

        return map.get(field);
    }

    public static void preload(Class<?> clazz) {
        CACHE.computeIfAbsent(clazz, MethodAccessorCache::build);
    }

    private static Map<String, PropertyAccessor> build(Class<?> clazz) {
        Map<String, TempAccessor> temp = new HashMap<>();
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // ===== scan methods (getter/setter)
        for (Method m : clazz.getMethods()) {
            try {
                if (isGetter(m)) {
                    String field = extractField(m.getName());

                    temp.computeIfAbsent(field, k -> new TempAccessor())
                        .getter = lookup.unreflect(m)
                            .asType(MethodType.methodType(Object.class, Object.class));

                } else if (isSetter(m)) {
                    String field = extractField(m.getName());

                    temp.computeIfAbsent(field, k -> new TempAccessor())
                        .setter = lookup.unreflect(m)
                            .asType(MethodType.methodType(void.class, Object.class, Object.class));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        // ===== fallback: VarHandle for direct field
        try {
            MethodHandles.Lookup privateLookup =
                MethodHandles.privateLookupIn(clazz, lookup);

            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                for (var f : current.getDeclaredFields()) {
                    String field = f.getName();

                    temp.computeIfAbsent(field, k -> new TempAccessor());

                    TempAccessor t = temp.get(field);

                    if (t.getter == null || t.setter == null) {
                        VarHandle vh = privateLookup.findVarHandle(
                            current, field, f.getType());

                        if (t.getter == null) {
                            t.getter = vh.toMethodHandle(VarHandle.AccessMode.GET)
                                .asType(MethodType.methodType(Object.class, Object.class));
                        }

                        if (t.setter == null) {
                            t.setter = vh.toMethodHandle(VarHandle.AccessMode.SET)
                                .asType(MethodType.methodType(void.class, Object.class, Object.class));
                        }
                    }
                }
                current = current.getSuperclass();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // ===== finalize
        Map<String, PropertyAccessor> result = new HashMap<>();

        for (Map.Entry<String, TempAccessor> e : temp.entrySet()) {
            TempAccessor t = e.getValue();
            if (t.getter != null || t.setter != null) {
                result.put(e.getKey(), new MethodAccessor(t.getter, t.setter));
            }
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

