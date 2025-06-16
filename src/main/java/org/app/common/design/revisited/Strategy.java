package org.app.common.design.revisited;

import java.util.function.Function;

@FunctionalInterface
public interface Strategy<I, O> {
    O execute(I input);

    static <I, O> Strategy<I, O> fromFunction(Function<I, O> function) {
        return function::apply;
    }
}
