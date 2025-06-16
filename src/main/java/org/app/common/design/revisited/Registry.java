package org.app.common.design.revisited;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Registry<K, V> {
    private final Map<K, V> instances = new ConcurrentHashMap<>(32);
    private final Map<K, Supplier<V>> factories = new ConcurrentHashMap<>(32);

    public void register(K key, V instance) {
        instances.put(key, instance);
    }

    public void registerFactory(K key, Supplier<V> factory) {
        factories.put(key, factory);
    }

    public Optional<V> get(K key) {
        V instance = instances.get(key);
        if (instance == null && factories.containsKey(key)) {
            instance = factories.get(key).get();
            instances.put(key, instance);
        }
        return Optional.ofNullable(instance);
    }
}