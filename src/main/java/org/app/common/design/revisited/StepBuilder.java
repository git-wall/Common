package org.app.common.design.revisited;

import java.util.function.Consumer;

public class StepBuilder<T> {

    private final T instance;

    private StepBuilder(T instance) {
        this.instance = instance;
    }

    public static <T> StepBuilder<T> of(T instance) {
        return new StepBuilder<>(instance);
    }

    public StepBuilder<T> step(Consumer<T> consumer) {
        consumer.accept(instance);
        return this;
    }

    public T build() {
        return instance;
    }
}
