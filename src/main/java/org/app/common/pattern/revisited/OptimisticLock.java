package org.app.common.pattern.revisited;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public class OptimisticLock<T> {
    private final AtomicReference<VersionedValue<T>> ref;

    public OptimisticLock(T initial) {
        this.ref = new AtomicReference<>(new VersionedValue<>(initial, 0L));
    }

    public T modify(UnaryOperator<T> operation) {
        VersionedValue<T> current;
        VersionedValue<T> next;
        do {
            current = ref.get();
            next = new VersionedValue<>(
                operation.apply(current.value),
                current.version + 1L
            );
        } while (!ref.compareAndSet(current, next));
        
        return next.value;
    }

    private static class VersionedValue<T> {
        final T value;
        final long version;

        VersionedValue(T value, long version) {
            this.value = value;
            this.version = version;
        }
    }
}