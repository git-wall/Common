package org.app.common.provider;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class Provider {
    public static <T> Predicate<T> alwaysTrue() {
        return t -> true;
    }

    public static <T> Predicate<T> alwaysFalse() {
        return t -> false;
    }

    public static <T> Predicate<T> isNull() {
        return Objects::isNull;
    }

    public static <T> Predicate<T> isNotNull() {
        return Objects::nonNull;
    }

    public static <T> Predicate<T> isEqualTo(T value) {
        return t -> Objects.equals(t, value);
    }

    public static <T, R> Function<T, String> thenToString(Function<T, R> function) {
        return t -> function.andThen(Objects::toString).apply(t);
    }
}
