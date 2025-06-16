package org.app.common.design.revisited;

import java.util.function.Function;

public class Bridge<T, R> {
    private final Function<T, R> implementation;

    public Bridge(Function<T, R> implementation) {
        this.implementation = implementation;
    }

    public R execute(T input) {
        return implementation.apply(input);
    }

    public <V> Bridge<T, V> extend(Function<R, V> extension) {
        return new Bridge<>(implementation.andThen(extension));
    }
}