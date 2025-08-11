package org.app.common.pipeline.v0;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Pipeline<T> {
    private final T data;

    public Pipeline(T data) {
        this.data = data;
    }

    public static <T> Pipeline<T> from(T source) {
        return new Pipeline<>(source);
    }

    public Pipeline<T> where(Predicate<T> predicate) {
        Optional<T> optional = Optional.of(data).filter(predicate);
        return optional.map(Pipeline::new)
                .orElseGet(() -> new Pipeline<>(null));
    }

    public <R> Pipeline<R> map(Function<T, R> transformer) {
        return Optional.of(data)
                .map(d -> new Pipeline<>(transformer.apply(d)))
                .orElseGet(() -> new Pipeline<>(null));
    }

    public Pipeline<T> then(Consumer<T> consumer) {
        consumer.accept(data);
        return this;
    }

    public static <T> Pipeline<T> sink(T source) {
        return new Pipeline<>(source);
    }

    public <R> Pipeline<R> sink(Function<T, R> function) {
        return new Pipeline<>(function.apply(data));
    }

    public T get() {
        return data;
    }
}