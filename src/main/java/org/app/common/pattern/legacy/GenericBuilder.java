package org.app.common.pattern.legacy;

import java.util.function.Consumer;

/**
 * Builder Pattern - EXAMPLE
 * <pre>{@code
 * Letter letter = GenericBuilder.of(new Letter())
 *     .with(l -> l.setFrom("John"))
 *     .with(l -> l.setTo("Jane"))
 *     .with(l -> l.setBody("Hello!"))
 *     .build();
 * }</pre>
 * */
public class GenericBuilder<T> {

    private final T instance;

    private GenericBuilder(T instance) {
        this.instance = instance;
    }

    public static <T> GenericBuilder<T> of(T instance) {
        return new GenericBuilder<>(instance);
    }

    public GenericBuilder<T> with(Consumer<T> consumer) {
        consumer.accept(instance);
        return this;
    }

    public T build() {
        return instance;
    }
}
