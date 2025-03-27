package org.app.common.pattern.revisited;

import java.util.function.Function;

public class Currying<T, U, R> {
    private final Function<T, Function<U, R>> curried;

    private Currying(Function<T, Function<U, R>> curried) {
        this.curried = curried;
    }

    public static <T, U, R> Currying<T, U, R> of(Function<T, Function<U, R>> function) {
        return new Currying<>(function);
    }

    public Function<U, R> apply(T t) {
        return curried.apply(t);
    }

    public R apply(T t, U u) {
        return curried.apply(t).apply(u);
    }

    public static <T, U, R> Function<T, Function<U, R>> curry(
            Function<Tuple<T, U>, R> function) {
        return t -> u -> function.apply(new Tuple<>(t, u));
    }

    private static class Tuple<T, U> {
        final T first;
        final U second;

        Tuple(T first, U second) {
            this.first = first;
            this.second = second;
        }
    }
}