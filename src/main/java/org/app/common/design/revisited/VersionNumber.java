package org.app.common.design.revisited;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class VersionNumber<T> {
    private final AtomicLong version = new AtomicLong(0L);
    private volatile T current;

    public VersionNumber(T initial) {
        this.current = initial;
    }

    public long getVersion() {
        return version.get();
    }

    public T get() {
        return current;
    }

    public synchronized T update(Function<T, T> updater) {
        T newValue = updater.apply(current);
        version.incrementAndGet();
        current = newValue;
        return newValue;
    }

    public boolean isNewer(long otherVersion) {
        return version.get() > otherVersion;
    }
}