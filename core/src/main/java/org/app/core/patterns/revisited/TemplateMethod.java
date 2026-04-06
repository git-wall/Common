package org.app.core.patterns.revisited;

import java.util.function.Function;

public class TemplateMethod<T, R> {
    private final Function<T, R> strategy;

    protected TemplateMethod(Function<T, R> strategy) {
        this.strategy = strategy;
    }

    public static <T, R> TemplateMethod<T, R> action(Function<T, R> strategy) {
        return new TemplateMethod<>(strategy);
    }

    public R executeFrom(T t) {
        return strategy.apply(t);
    }
}
