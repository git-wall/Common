package org.app.common.design.revisited;

import java.util.function.Supplier;

public class DirtyFlag<T> {
    private T value;
    private boolean dirty = true;
    private final Supplier<T> calculator;

    public DirtyFlag(Supplier<T> calculator) {
        this.calculator = calculator;
    }

    public void invalidate() {
        dirty = true;
    }

    public T getValue() {
        if (dirty) {
            value = calculator.get();
            dirty = false;
        }
        return value;
    }
}