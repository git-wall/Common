package org.app.core.accessor.lambda;

import org.app.core.accessor.PropertyAccessor;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class LambdaAccessor implements PropertyAccessor {

    private final Function<Object, Object> getter;
    private final BiConsumer<Object, Object> setter;

    public LambdaAccessor(Function<Object, Object> getter, BiConsumer<Object, Object> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Object get(Object target) {
        return getter != null ? getter.apply(target) : null;
    }

    @Override
    public void set(Object target, Object value) {
        if (setter != null) setter.accept(target, value);
    }
}
