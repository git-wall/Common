package org.app.common.pattern.revisited;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventPublisher<T> {
    private final Map<Class<?>, CopyOnWriteArrayList<Consumer<T>>> subscribers = new ConcurrentHashMap<>(32);

    public void subscribe(Class<? extends T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public void publish(T event) {
        subscribers.getOrDefault(event.getClass(), new CopyOnWriteArrayList<>())
                .forEach(handler -> handler.accept(event));
    }

    public void unsubscribe(Class<? extends T> eventType, Consumer<T> handler) {
        subscribers.computeIfPresent(eventType, (k, v) -> {
            v.remove(handler);
            return v;
        });
    }
}