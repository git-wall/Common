package org.app.core.accessor.lambda;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class LambdaFactory {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @SuppressWarnings("unchecked")
    public static Function<Object, Object> createGetter(Method method) {
        try {
            MethodHandle mh = LOOKUP.unreflect(method);

            CallSite site = LambdaMetafactory.metafactory(
                LOOKUP,
                "apply",
                MethodType.methodType(Function.class),
                MethodType.methodType(Object.class, Object.class), // erased
                mh,
                mh.type()
            );

            return (Function<Object, Object>) site.getTarget().invokeExact();

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static BiConsumer<Object, Object> createSetter(Method method) {
        try {
            MethodHandle mh = LOOKUP.unreflect(method);

            CallSite site = LambdaMetafactory.metafactory(
                LOOKUP,
                "accept",
                MethodType.methodType(BiConsumer.class),
                MethodType.methodType(void.class, Object.class, Object.class), // erased
                mh,
                mh.type()
            );

            return (BiConsumer<Object, Object>) site.getTarget().invokeExact();

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
