package org.app.common.pattern.revisited;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ExtensionObjects<T> {
    private final Map<Class<?>, Function<T, ?>> extensions = new ConcurrentHashMap<>();
    private final T target;

    public ExtensionObjects(T target) {
        this.target = target;
    }

    public <E> void registerExtension(Class<E> type, Function<T, E> provider) {
        extensions.put(type, provider);
    }

    @SuppressWarnings("unchecked")
    public <E> Optional<E> getExtension(Class<E> type) {
        Function<T, ?> provider = extensions.get(type);
        return provider != null ? Optional.of((E) provider.apply(target)) : Optional.empty();
    }
}