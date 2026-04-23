package org.app.core.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Loop {

    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T, K> keyExtractor) {
        Map<K, List<T>> result = new HashMap<>();

        for (T item : list) {
            K key = keyExtractor.apply(item);

            List<T> bucket = result.computeIfAbsent(key, k -> new ArrayList<>());
            bucket.add(item);
        }

        return result;
    }

    /**
     * Example:
     * <pre>{@code
     * Map<Long, BigDecimal> totalAmountByUser = groupSum(orders, Order::getUserId, Order::getAmount);
     * }</pre>
     */
    public static <T, K> Map<K, BigDecimal> groupSum(
        List<T> list,
        Function<T, K> keyExtractor,
        Function<T, BigDecimal> valueExtractor
    ) {
        Map<K, BigDecimal> result = new HashMap<>();

        for (T item : list) {
            K key = keyExtractor.apply(item);
            BigDecimal val = valueExtractor.apply(item);

            result.merge(key, val, BigDecimal::add);
        }
        return result;
    }

    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return false;
        }

        for (T item : collection) {
            if (predicate.test(item)) {
                return true;
            }
        }

        return false;
    }

    public static <T> T findFirst(Collection<T> collection, Predicate<T> condition) {
        if (collection == null || condition == null) {
            return null;
        }

        for (T item : collection) {
            if (condition.test(item)) {
                return item;
            }
        }

        return null;
    }

    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        List<T> list = new ArrayList<>();
        for (T item : collection) {
            if (predicate.test(item)) {
                list.add(item);
            }
        }
        return list;
    }

    public static <T, R> List<R> map(Collection<T> collection, Function<T, R> map) {
        List<R> list = new ArrayList<>(collection.size());
        for (T item : collection) {
            R r = map.apply(item);
            list.add(r);
        }
        return list;
    }

    public static <T, R> List<R> flatMap(Collection<T> collection, Function<T, Collection<R>> map) {
        List<R> list = new ArrayList<>();
        for (T item : collection) {
            Collection<R> r = map.apply(item);
            list.addAll(r);
        }
        return list;
    }

    public static <T> void fill(Collection<T> collection, Consumer<T> fill) {
        for (T t : collection) {
            fill.accept(t);
        }
    }

    public static <T, C> void fillWithContext(Collection<T> collection, C context, BiConsumer<T, C> fill) {
        for (T t : collection) {
            fill.accept(t, context);
        }
    }

    public static <T, R> void drainTo(Collection<T> collection1, Collection<R> collection2, Function<T, R> map) {
        for (T e : collection1) {
            collection2.add(map.apply(e));
        }
    }

    public static <K, V> Map<K, V> toMap(List<V> list, Function<V, K> keyMapper) {
        Map<K, V> map = new HashMap<>(list.size());
        for (V value : list) {
            K key = keyMapper.apply(value);
            map.put(key, value);
        }
        return map;
    }
}
