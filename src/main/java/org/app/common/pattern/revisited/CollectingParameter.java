package org.app.common.pattern.revisited;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CollectingParameter<T, R> {
    private final List<T> collected = new ArrayList<>();
    private final Function<List<T>, R> processor;

    public CollectingParameter(Function<List<T>, R> processor) {
        this.processor = processor;
    }

    public void collect(T item) {
        collected.add(item);
    }

    public void collectAll(List<T> items) {
        collected.addAll(items);
    }

    public R process() {
        return processor.apply(new ArrayList<>(collected));
    }

    public void forEach(Consumer<T> action) {
        collected.forEach(action);
    }
}