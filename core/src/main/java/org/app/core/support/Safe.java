package org.app.core.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Safe {

    public static <K, V> V mapGet(Map<K, V> map, K key) {
        return map == null ? null : map.get(key);
    }

    public static <T, R> R get(T source, Function<T, R> function) {
        return source == null ? null : function.apply(source);
    }

    public static <T, R> R getOrElse(T source, Function<T, R> function, R defaultValue) {
        return source == null ? defaultValue : function.apply(source);
    }
}
