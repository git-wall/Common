package org.app.common.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Pipeline<T> {
    private final List<T> data;

    private Pipeline(List<T> data) {
        this.data = new ArrayList<>(data); // Defensive copy
    }

    // Factory method to create pipeline
    public static <T> Pipeline<T> from(List<T> source) {
        return new Pipeline<>(source);
    }

    // Filter operation
    public Pipeline<T> filter(Predicate<T> predicate) {
        List<T> filtered = data.stream().filter(predicate).collect(Collectors.toList());
        return new Pipeline<>(filtered);
    }

    // Transform operation
    public <R> Pipeline<R> transform(Function<T, R> transformer) {
        List<R> transformed = data.stream()
                .map(transformer)
                .collect(Collectors.toCollection(() -> new ArrayList<>(data.size()))); // Pre-allocate
        return new Pipeline<>(transformed);
    }

    // Sink operation
    public void sink(Consumer<T> consumer) {
        for (T item : data) {
            consumer.accept(item);
        }
    }

    // Collect to list as a terminal operation
    public List<T> collect() {
        return new ArrayList<>(data);
    }
}
