package org.app.common.pattern.revisited;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Filterer<T> {
    private final List<Predicate<T>> filters = new ArrayList<>(8);

    public Filterer<T> add(Predicate<T> filter) {
        filters.add(filter);
        return this;
    }

    public Filterer<T> addIf(boolean condition, Predicate<T> filter) {
        if (condition) {
            filters.add(filter);
        }
        return this;
    }

    public List<T> filter(List<T> items) {
        return items.stream()
                .filter(item -> filters.stream().allMatch(f -> f.test(item)))
                .collect(Collectors.toList());
    }

    public boolean test(T item) {
        return filters.stream().allMatch(f -> f.test(item));
    }

    public void clear() {
        filters.clear();
    }
}