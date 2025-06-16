package org.app.common.design.legacy;

import lombok.Getter;
import lombok.SneakyThrows;

import javax.management.ServiceNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ServiceLocator {
    @Getter
    private static final ServiceLocator instance;
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, Supplier<?>> factories = new ConcurrentHashMap<>(32);

    static {
        instance = new ServiceLocator();
    }

    private ServiceLocator() {}

    public <T> void register(Class<T> serviceType, T serviceInstance) {
        services.put(serviceType, serviceInstance);
    }

    public <T> void registerFactory(Class<T> serviceType, Supplier<T> factory) {
        factories.put(serviceType, factory);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> serviceType) {
        // Check for direct service instance
        Object service = services.get(serviceType);
        if (service != null) {
            return (T) service;
        }

        // Check for factory
        Supplier<?> factory = factories.get(serviceType);
        if (factory != null) {
            T instance = (T) factory.get();
            services.put(serviceType, instance); // Cache the instance
            return instance;
        }

        throw new ServiceNotFoundException("No service registered for type: " + serviceType.getName());
    }

    public void clear() {
        services.clear();
        factories.clear();
    }
}
