package org.app.common.algorithms;

import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ArrayUtils {

    public static final int INSERTION_SORT_SIZE = 7;

    public static <T extends Comparable<T>> T[] sortToArray(Collection<T> collection) {
        T[] array = collection.toArray((T[]) new Comparable[collection.size()]);
        T[] clone = array.clone();
        merge(clone, array, 0, array.length, 0);
        return array;
    }

    @SneakyThrows
    public static <T extends Comparable<T>> List<T> sort(Collection<T> c) {
        T[] array = c.toArray((T[]) new Comparable[c.size()]);
        T[] clone = array.clone();
        merge(clone, array, 0, array.length, 0);
        return Arrays.asList(array);
    }

    public static <T extends Comparable<T>> void merge(T[] src, T[] dest, int low, int high, int offset) {
        int length = high - low;

        if (length <= INSERTION_SORT_SIZE) {
            insert(dest, low, high);
            return;
        }

        int destLow = low;
        int destHigh = high;
        low += offset;
        high += offset;
        int mid = (low + high) >>> 1;

        merge(dest, src, low, mid, -offset);
        merge(dest, src, mid, high, -offset);

        // already sorted
        if (src[mid - 1].compareTo(src[mid]) < 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && src[p].compareTo(src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    /**
     * Good for small, ok if size is <= 7
     *
     * @param dest size <= 7
     * @param low  min size
     * @param high max size
     */
    public static <T extends Comparable<T>> void insert(T[] dest, int low, int high) {
        for (int i = low; i < high; ++i)
            for (int j = i; j > low && (dest[j - 1]).compareTo(dest[j]) > 0; j--)
                swap(dest, i, j - 1);
    }

    public static <T> void swap(T[] x, int a, int b) {
        T t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
