package org.app.common.utils;

import org.app.common.design.revisited.LazyLoader;

import java.lang.reflect.Proxy;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProxyUtils {

    // Lazy Proxy
    public static <T> LazyLoader<T> lazy(Supplier<T> supplier) {
        return LazyLoader.of(supplier);
    }

    // Logging Proxy
    public static <T> T logProxy(Class<T> interfaceType, T target) {
        return createProxy(interfaceType, (proxy, method, args) -> {
            System.out.println("[LOG] Calling: " + method.getName());
            return method.invoke(target, args);
        });
    }

    // Security Proxy (mocked simple check)
    public static <T> T secProxy(Class<T> interfaceType, T target, Supplier<Boolean> hasPermission) {
        return createProxy(interfaceType, (proxy, method, args) -> {
            if (!hasPermission.get()) {
                throw new SecurityException("Access denied to method: " + method.getName());
            }
            return method.invoke(target, args);
        });
    }

    // Caching Proxy (simple in-memory cache)
    public static <T> T cachingProxy(Class<T> interfaceType,
                                     T target,
                                     Function<String, Object> cacheGet,
                                     BiConsumer<String, Object> cachePut) {
        return createProxy(interfaceType, (proxy, method, args) -> {
            String key = method.getName() + java.util.Arrays.toString(args);
            Object result = cacheGet.apply(key);
            if (result == null) {
                result = method.invoke(target, args);
                cachePut.accept(key, result);  // dùng BiConsumer ở đây
            }
            return result;
        });
    }

    // Generic dynamic proxy creator
    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> interfaceType, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler
        );
    }
}
