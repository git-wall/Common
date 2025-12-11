package org.app.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArrayUtils {
    private ArrayUtils() {
    }

    /**
     * Root of thing to optimize about combine an array with layer near JVM
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] combine(T[] src1, T[] src2) {
        Object[] combineArray = new Object[src1.length + src2.length];
        System.arraycopy(src1, 0, combineArray, 0, src1.length);
        System.arraycopy(src2, 0, combineArray, src1.length, src2.length);

        return (T[]) combineArray;
    }

    public static <T> boolean anyMatch(T[] array, Predicate<T> predicate) {
        if (array == null) {
            return false;
        }

        for (T element : array) {
            if (predicate.test(element)) {
                return true;
            }
        }

        return false;
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

    public static <T> T findFirst(T[] array, Predicate<T> condition) {
        if (array == null || condition == null) {
            return null;
        }

        for (T element : array) {
            if (condition.test(element)) {
                return element;
            }
        }

        return null;
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

    public static <T, R> List<R> map(T[] array, Function<T, R> map) {
        if (array == null || map == null) {
            return null;
        }

        List<R> list = new ArrayList<>(array.length);
        for (T e : array) {
            R r = map.apply(e);
            list.add(r);
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

    /**
     * <h3><strong>Attention: Not use if array < 10</strong><br>
     *
     * <h4>Benefit</h4>
     * <strong>Performance</strong>: Reduces overhead, leverages CPU and JIT optimizations,
     * and improves cache usage—ideal for tight loops over large arrays. <br><br>
     * <strong>Correctness</strong>: The cleanup loop ensures no elements are missed or processed twice, handling arrays of any length safely. <br><br>
     * <strong>Maintainability</strong>: The code is still readable, with a clear intent (unrolling for performance),
     * and the comment aids understanding.
     */
    public static <T> void processArray(T[] array, Consumer<T> process) {
        if (array == null || process == null) {
            return;
        }

        // Main unrolled loop: process 4 elements at a time
        for (int i = 0; i < array.length - 4; i += 4) {
            process.accept(array[i]);
            process.accept(array[i + 1]);
            process.accept(array[i + 2]);
            process.accept(array[i + 3]);
        }

        // Cleanup loop: handle remaining elements
        for (int i = (array.length & ~3); i < array.length; i++) {
            process.accept(array[i]);
        }
    }
}
