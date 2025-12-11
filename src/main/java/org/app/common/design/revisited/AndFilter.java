package org.app.common.design.revisited;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AndFilter<T> {
    private Predicate<T> predicate;

    public AndFilter() {
        this.predicate = item -> true; // Start with always true
    }

    private AndFilter(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    public static <T> AndFilter<T> create() {
        return new AndFilter<>();
    }

    // Add condition using .and()
    public AndFilter<T> add(Predicate<T> condition) {
        this.predicate = this.predicate.and(condition);
        return this;
    }

    public Predicate<T> build() {
        return predicate;
    }

    public List<T> apply(List<T> list) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }
}
