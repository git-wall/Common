package org.app.common.design.revisited;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class MarkerInterface<T> {
    private final Map<Class<?>, Predicate<T>> validators = new HashMap<>();

    public void register(Class<?> markerInterface, Predicate<T> validator) {
        validators.put(markerInterface, validator);
    }

    public boolean isValid(Class<?> markerInterface, T instance) {
        Predicate<T> validator = validators.get(markerInterface);
        return validator != null && validator.test(instance);
    }
}