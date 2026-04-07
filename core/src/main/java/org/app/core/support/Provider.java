package org.app.core.support;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class for providing common predicates and functions for functional programming.
 * <p>
 * This class contains static methods to create reusable predicates and functions,
 * such as always-true/false predicates, null checks, equality checks, and dynamic filters.
 * </p>
 * <p>
 * The class is designed to be non-instantiable and provides static utility methods.
 * </p>
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Provider {

    /**
     * Returns a predicate that always evaluates to {@code true}.
     *
     * @param <T> the type of the input to the predicate
     * @return a predicate that always evaluates to {@code true}
     */
    public static <T> Predicate<T> alwaysTrue() {
        return t -> true;
    }

    /**
     * Returns a predicate that always evaluates to {@code false}.
     *
     * @param <T> the type of the input to the predicate
     * @return a predicate that always evaluates to {@code false}
     */
    public static <T> Predicate<T> alwaysFalse() {
        return t -> false;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the input is {@code null}.
     *
     * @param <T> the type of the input to the predicate
     * @return a predicate that evaluates to {@code true} if the input is {@code null}
     */
    public static <T> Predicate<T> isNull() {
        return Objects::isNull;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the input is not {@code null}.
     *
     * @param <T> the type of the input to the predicate
     * @return a predicate that evaluates to {@code true} if the input is not {@code null}
     */
    public static <T> Predicate<T> isNotNull() {
        return Objects::nonNull;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the input is equal to the specified value.
     *
     * @param <T>   the type of the input to the predicate
     * @param value the value to compare against
     * @return a predicate that evaluates to {@code true} if the input is equal to the specified value
     */
    public static <T> Predicate<T> isEqualTo(T value) {
        return t -> Objects.equals(t, value);
    }

    /**
     * Returns a function that applies the given function and converts the result to a {@link String}.
     *
     * @param <T>      the type of the input to the function
     * @param <R>      the type of the result of the given function
     * @param function the function to apply
     * @return a function that applies the given function and converts the result to a {@link String}
     */
    public static <T, R> Function<T, String> thenToString(Function<T, R> function) {
        return t -> function.andThen(Objects::toString).apply(t);
    }

    /**
     * Returns a predicate that combines a list of predicates using logical AND.
     * <p>
     * If the list is empty, the returned predicate always evaluates to {@code true}.
     * </p>
     *
     * @param <T>  the type of the input to the predicates
     * @param list the list of predicates to combine
     * @return a predicate that combines the list of predicates using logical AND
     */
    public static <T> Predicate<T> dynamicFilter(List<Predicate<T>> list) {
        return list.stream().reduce(Predicate::and).orElse(alwaysTrue());
    }
}
