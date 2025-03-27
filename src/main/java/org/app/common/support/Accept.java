package org.app.common.support;

import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Accept {
    /**
     * The singleton instance of this utility class.
     */
    public static final Accept INSTANCE = new Accept();

    private Accept() {
    }

    /**
     * Invoke {@link Consumer#accept(Object)} with the value if the condition is true.
     * @param condition the condition.
     * @param value the value.
     * @param consumer the consumer.
     * @param <T> the value type.
     * @return this.
     */
    public <T> Accept ifCondition(boolean condition, T value, Consumer<T> consumer) {
        if (condition) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * Invoke {@link Consumer#accept(Object)} with the value if it is not null.
     * @param value the value.
     * @param consumer the consumer.
     * @param <T> the value type.
     * @return this.
     */
    public <T> Accept ifNotNull(@Nullable T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * Invoke {@link Consumer#accept(Object)} with the value if it is not null or empty.
     * @param value the value.
     * @param consumer the consumer.
     * @return this.
     */
    public Accept ifHasText(String value, Consumer<String> consumer) {
        if (StringUtils.hasText(value)) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * Invoke {@link Consumer#accept(Object)} with the value if it is not null or empty.
     * @param value the value.
     * @param consumer the consumer.
     * @param <T> the value type.
     * @return this.
     */
    public <T> Accept ifNotEmpty(List<T> value, Consumer<List<T>> consumer) {
        if (!CollectionUtils.isEmpty(value)) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * Invoke {@link Consumer#accept(Object)} with the value if it is not null or empty.
     * @param value the value.
     * @param consumer the consumer.
     * @param <T> the value type.
     * @return this.
     */
    public <T> Accept ifNotEmpty(T[] value, Consumer<T[]> consumer) {
        if (!ObjectUtils.isEmpty(value)) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * Invoke {@link BiConsumer#accept(Object, Object)} with the arguments if the
     * condition is true.
     * @param condition the condition.
     * @param t1 the first consumer argument
     * @param t2 the second consumer argument
     * @param consumer the consumer.
     * @param <T1> the first argument type.
     * @param <T2> the second argument type.
     * @return this.
     */
    public <T1, T2> Accept ifCondition(boolean condition, T1 t1, T2 t2, BiConsumer<T1, T2> consumer) {
        if (condition) {
            consumer.accept(t1, t2);
        }
        return this;
    }

    /**
     * Invoke {@link BiConsumer#accept(Object, Object)} with the arguments if the t2
     * argument is not null.
     * @param t1 the first argument
     * @param t2 the second consumer argument
     * @param consumer the consumer.
     * @param <T1> the first argument type.
     * @param <T2> the second argument type.
     * @return this.
     */
    public <T1, T2> Accept ifNotNull(T1 t1, T2 t2, BiConsumer<T1, T2> consumer) {
        if (t2 != null) {
            consumer.accept(t1, t2);
        }
        return this;
    }

    /**
     * Invoke {@link BiConsumer#accept(Object, Object)} with the arguments if the value
     * argument is not null or empty.
     * @param t1 the first consumer argument.
     * @param value the second consumer argument
     * @param <T> the first argument type.
     * @param consumer the consumer.
     * @return this.
     */
    public <T> Accept ifHasText(T t1, String value, BiConsumer<T, String> consumer) {
        if (StringUtils.hasText(value)) {
            consumer.accept(t1, value);
        }
        return this;
    }
}
