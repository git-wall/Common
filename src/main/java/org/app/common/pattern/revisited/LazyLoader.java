package org.app.common.pattern.revisited;

import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <img src="https://java-design-patterns.com/assets/img/lazy-loading-sequence-diagram.dc18e6ce.png" alt="LazyLoader">
 * <p>
 * Lazy loading is a design pattern that postpones the initialization of an object until the point at which it is needed.
 * This can help to improve performance and reduce memory usage, especially when dealing with large objects or resources.
 * </p>
 *
 * Usage:
 * <pre>{@code
 * LazyLoader<EntityUserLog> loader = LazyLoader.of(EntityUserLog::new);
 * }
 *
 * @param <T> the type of the object to be lazily loaded
 */
public class LazyLoader<T> {
    private final Supplier<T> supplier;
    private T value;
    @Getter
    private volatile boolean initialized = false;

    private LazyLoader(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazyLoader<T> of(Supplier<T> supplier) {
        return new LazyLoader<>(supplier);
    }

    public synchronized T get() {
        if (!initialized) {
            value = supplier.get();
            initialized = true;
        }
        return value;
    }

    public <R> LazyLoader<R> map(Function<T, R> mapper) {
        return LazyLoader.of(() -> mapper.apply(get()));
    }
}

