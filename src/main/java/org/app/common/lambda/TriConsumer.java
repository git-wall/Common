package org.app.common.lambda;

/**
 * Represents an operation that accepts 3 input arguments and returns no
 * result. This is 3-arity specialization of {@link java.util.function.Consumer}.
 * Unlike most other functional interfaces, {@code TriConsumer} is expected
 * to operate via side effects.
 *
 * @see java.util.function.Consumer
 * @see java.util.function.BiConsumer
 */

@FunctionalInterface
public interface TriConsumer<T, U, V> {
    /**
     * @param t first param
     * @param u second param
     * @param v third param
     * */
    void accept(T t, U u, V v);
}
