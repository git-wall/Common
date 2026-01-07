package org.app.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CrowdUtils {

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
        collection.forEach(fill);
    }
}
