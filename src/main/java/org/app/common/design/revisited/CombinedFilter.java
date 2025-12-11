package org.app.common.design.revisited;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CombinedFilter<T> {
    private Predicate<T> predicate;

    private CombinedFilter(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    public static <T> CombinedFilter<T> start() {
        return new CombinedFilter<>(item -> true);
    }

    public static <T> CombinedFilter<T> from(Predicate<T> predicate) {
        return new CombinedFilter<>(predicate);
    }

    // Add with AND logic
    public CombinedFilter<T> and(Predicate<T> condition) {
        this.predicate = this.predicate.and(condition);
        return this;
    }

    // Add with OR logic
    public CombinedFilter<T> or(Predicate<T> condition) {
        this.predicate = this.predicate.or(condition);
        return this;
    }

    // Add NOT logic
    public CombinedFilter<T> not(Predicate<T> condition) {
        this.predicate = this.predicate.and(condition.negate());
        return this;
    }

    // Combine with another filter using AND
    public CombinedFilter<T> andFilter(AndFilter<T> filter) {
        this.predicate = this.predicate.and(filter.build());
        return this;
    }

    // Combine with another filter using OR
    public CombinedFilter<T> orFilter(OrFilter<T> filter) {
        this.predicate = this.predicate.or(filter.build());
        return this;
    }

    public Predicate<T> build() {
        return predicate;
    }

    public List<T> apply(List<T> list) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }
}
