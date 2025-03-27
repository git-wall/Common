package org.app.common.pattern.revisited;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FunctionComposition<T, R> {
    private final List<Function<Object, Object>> pipeline = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public <V> FunctionComposition<T, V> add(Function<R, V> function) {
        pipeline.add((Function<Object, Object>) function);
        return (FunctionComposition<T, V>) this;
    }

    @SuppressWarnings("unchecked")
    public R execute(T input) {
        Object result = input;
        for (Function<Object, Object> function : pipeline) {
            result = function.apply(result);
        }
        return (R) result;
    }

    public static <T, R> FunctionComposition<T, R> start(Function<T, R> initial) {
        FunctionComposition<T, R> composition = new FunctionComposition<>();
        composition.pipeline.add((Function<Object, Object>) initial);
        return composition;
    }
}