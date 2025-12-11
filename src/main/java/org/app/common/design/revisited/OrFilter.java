package org.app.common.design.revisited;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OrFilter<T> {
    private Predicate<T> predicate;

    public OrFilter() {
        this.predicate = item -> false; // Start with always false
    }

    private OrFilter(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    public static <T> OrFilter<T> create() {
        return new OrFilter<>();
    }

    // Add condition using .or()
    public OrFilter<T> add(Predicate<T> condition) {
        this.predicate = this.predicate.or(condition);
        return this;
    }

    public Predicate<T> build() {
        return predicate;
    }

    public List<T> apply(List<T> list) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }
}

