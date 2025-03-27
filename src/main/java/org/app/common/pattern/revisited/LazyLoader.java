package org.app.common.pattern.revisited;

import lombok.Getter;

import java.util.function.Function;

public class LazyLoader<T> {
    private final T value;
    @Getter
    private volatile boolean initialized = false;

    private LazyLoader(T value) {
        this.value = value;
    }

    public static <T> LazyLoader<T> of(T value) {
        return new LazyLoader<>(value);
    }

    public synchronized T get() {
        if (!initialized) {
            initialized = true;
        }
        return value;
    }

    public <R> LazyLoader<R> map(Function<T, R> mapper) {
        return LazyLoader.of(mapper.apply(value));
    }
}