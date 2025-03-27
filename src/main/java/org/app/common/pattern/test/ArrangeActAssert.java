package org.app.common.pattern.test;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ArrangeActAssert<T, R> {
    private final Supplier<T> arrange;
    private final Function<T, R> act;
    private final Predicate<R> asert;

    public ArrangeActAssert(
            Supplier<T> arrange,
            Function<T, R> act,
            Predicate<R> asert) {
        this.arrange = arrange;
        this.act = act;
        this.asert = asert;
    }

    public boolean execute() {
        try {
            T context = arrange.get();

            R result = act.apply(context);

            return asert.test(result);
        } catch (Exception e) {
            return false;
        }
    }
}