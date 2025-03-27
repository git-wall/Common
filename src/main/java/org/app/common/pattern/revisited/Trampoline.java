package org.app.common.pattern.revisited;

import java.util.stream.Stream;

@FunctionalInterface
public interface Trampoline<T> {
    Trampoline<T> next();

    default boolean complete() {
        return false;
    }

    default T result() {
        throw new UnsupportedOperationException();
    }

    default T evaluate() {
        return Stream.iterate(this, Trampoline::next)
                .filter(Trampoline::complete)
                .findFirst()
                .map(Trampoline::result)
                .orElseThrow();
    }

    static <T> Trampoline<T> done(final T result) {
        return new Trampoline<>() {
            @Override
            public boolean complete() {
                return true;
            }

            @Override
            public T result() {
                return result;
            }

            @Override
            public Trampoline<T> next() {
                throw new UnsupportedOperationException();
            }
        };
    }

    static <T> Trampoline<T> more(final Trampoline<T> next) {
        return () -> next;
    }
}