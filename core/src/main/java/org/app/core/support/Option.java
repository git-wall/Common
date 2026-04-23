package org.app.core.support;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utility class for functional programming operations using {@link Optional}.
 * <p>
 * This class provides static methods to simplify common operations such as mapping,
 * filtering, and handling exceptions in a functional style.
 * </p>
 * <p>
 * The class is designed to be non-instantiable and provides static utility methods.
 * </p>
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Option {

    /**
     * Maps a value of type {@code T} to a value of type {@code R} using the provided mapper function.
     * <p>
     * If the mapper function returns {@code null}, an {@link IllegalStateException} is thrown.
     * </p>
     *
     * @param t      the input value
     * @param mapper the mapping function to apply
     * @param <T>    the type of the input value
     * @param <R>    the type of the result value
     * @return the result of applying the mapper function to the input value
     * @throws IllegalStateException if the mapper function returns {@code null}
     */
    public static <T, R> R mapper(T t, Function<T, R> mapper) {
        return Optional.of(t).map(mapper).orElseThrow(() -> new IllegalStateException("Mapper failed"));
    }

    /**
     * Maps a value of type {@code T} to a value of type {@code R} using the provided mapper function.
     * <p>
     * If the mapper function returns {@code null}, the exception provided by the {@code exceptionSupplier} is thrown.
     * </p>
     *
     * @param t                 the input value
     * @param mapper            the mapping function to apply
     * @param exceptionSupplier the supplier of the exception to throw if the mapper function returns {@code null}
     * @param <T>               the type of the input value
     * @param <R>               the type of the result value
     * @param <X>               the type of the exception to throw
     * @return the result of applying the mapper function to the input value
     */
    @SneakyThrows
    public static <T, R, X extends Throwable> R mapper(T t, Function<T, R> mapper, Supplier<? extends X> exceptionSupplier) {
        return Optional.of(t).map(mapper).orElseThrow(exceptionSupplier);
    }

    /**
     * Filters the input value using the provided predicate and returns it if the predicate evaluates to {@code true}.
     * <p>
     * If the predicate evaluates to {@code false}, an {@link IllegalStateException} is thrown with the provided error message.
     * </p>
     *
     * @param t      the input value
     * @param filter the predicate to apply for filtering
     * @param error  the error message for the exception if the predicate evaluates to {@code false}
     * @param <T>    the type of the input value
     * @return the input value if the predicate evaluates to {@code true}
     * @throws IllegalStateException if the predicate evaluates to {@code false}
     */
    public static <T> T filterThrow(T t, Predicate<T> filter, String error) {
        return Optional.ofNullable(t)
            .filter(filter)
            .orElseThrow(() -> new IllegalStateException(error));
    }

    /**
     * Filters the input value using the provided predicate and returns it if the predicate evaluates to {@code true}.
     * <p>
     * If the predicate evaluates to {@code false}, the {@code other} value is returned instead.
     * </p>
     *
     * @param t      the input value
     * @param filter the predicate to apply for filtering
     * @param other  the value to return if the predicate evaluates to {@code false}
     * @param <T>    the type of the input value
     * @return the input value if the predicate evaluates to {@code true}, otherwise the {@code other} value
     */
    public static <T> T filterOrElse(T t, Predicate<T> filter, T other) {
        return Optional.ofNullable(t)
            .filter(filter)
            .orElse(other);
    }
}
