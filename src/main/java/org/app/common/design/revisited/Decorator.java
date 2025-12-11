package org.app.common.design.revisited;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Decorator<T> {
    private Function<T, T> chain = UnaryOperator.identity();

    public Decorator<T> add(UnaryOperator<T> decorator) {
        chain = chain.andThen(decorator);
        return this;
    }

    public T apply(T input) {
        return chain.apply(input);
    }
}
