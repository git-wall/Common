package org.app.common.design.revisited;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ParameterObject {
    private final Map<String, Object> parameters = new HashMap<>();

    public <T> void set(String name, T value) {
        parameters.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String name, Class<T> type) {
        Object value = parameters.get(name);
        return type.isInstance(value) ? Optional.of((T) value) : Optional.empty();
    }

    public <T, R> Optional<R> map(String name, Class<T> type, Function<T, R> mapper) {
        return get(name, type).map(mapper);
    }

    public boolean has(String name) {
        return parameters.containsKey(name);
    }
}