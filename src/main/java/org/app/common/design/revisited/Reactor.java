package org.app.common.design.revisited;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Reactor<T> {
    private final Map<String, Consumer<T>> handlers = new ConcurrentHashMap<>();
    private final Map<String, Class<? extends T>> eventTypes = new ConcurrentHashMap<>();

    public void register(String eventType, Class<? extends T> eventClass, Consumer<T> handler) {
        eventTypes.put(eventType, eventClass);
        handlers.put(eventType, handler);
    }

    public void dispatch(String eventType, T event) {
        if (handlers.containsKey(eventType) && 
            eventTypes.get(eventType).isInstance(event)) {
            handlers.get(eventType).accept(event);
        }
    }

    public void unregister(String eventType) {
        handlers.remove(eventType);
        eventTypes.remove(eventType);
    }
}