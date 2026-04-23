package org.app.core.accessor.method;

import org.app.core.accessor.PropertyAccessor;

import java.lang.invoke.MethodHandle;

public final class MethodAccessor implements PropertyAccessor {

    private final MethodHandle getter;
    private final MethodHandle setter;

    MethodAccessor(MethodHandle getter, MethodHandle setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Object get(Object target) {
        if (getter == null) return null;
        try {
            return getter.invokeExact(target);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Object target, Object value) {
        if (setter == null) return;
        try {
            setter.invokeExact(target, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
