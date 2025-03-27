package org.app.common.pattern.revisited;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NakedObjects<T> {
    private final Map<String, Function<T, ?>> propertyGetters = new HashMap<>();
    private final Map<String, Method> actions = new HashMap<>();

    public void expose(String propertyName, Function<T, ?> getter) {
        propertyGetters.put(propertyName, getter);
    }

    public void registerAction(String actionName, Method method) {
        actions.put(actionName, method);
    }

    public Object getProperty(String propertyName, T target) {
        Function<T, ?> getter = propertyGetters.get(propertyName);
        if (getter == null) {
            throw new IllegalArgumentException("Property not found: " + propertyName);
        }
        return getter.apply(target);
    }

    public <R> R invokeAction(String actionName, T target, Object... args) throws Exception {
        Method method = actions.get(actionName);
        if (method == null) {
            throw new IllegalArgumentException("Action not found: " + actionName);
        }
        return (R) method.invoke(target, args);
    }
}