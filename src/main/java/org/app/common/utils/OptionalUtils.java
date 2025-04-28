package org.app.common.utils;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class OptionalUtils {
    public static <T, R> R mapper(T t, Function<T, R> mapper) {
        return Optional.of(t).map(mapper).orElseThrow(() -> new IllegalStateException("Error when mapper"));
    }

    public static <T> T filterThrow(T t, Predicate<T> filter, String error) {
        return Optional.ofNullable(t)
                .filter(filter)
                .orElseThrow(() -> new IllegalStateException(error));
    }

    public static <T> T filterAndOrElse(T t, Predicate<T> filter, T other) {
        return Optional.ofNullable(t)
                .filter(filter)
                .orElse(other);
    }
}
