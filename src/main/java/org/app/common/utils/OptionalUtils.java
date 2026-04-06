package org.app.common.utils;

import lombok.SneakyThrows;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OptionalUtils {

    public static <T, R> R mapper(T t, Function<T, R> mapper) {
        return Optional.of(t).map(mapper).orElseThrow(() -> new IllegalStateException("Mapper failed"));
    }

    @SneakyThrows
    public static <T, R, X extends Throwable> R mapper(T t, Function<T, R> mapper, Supplier<? extends X> exceptionSupplier) {
        return Optional.of(t).map(mapper).orElseThrow(exceptionSupplier);
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
