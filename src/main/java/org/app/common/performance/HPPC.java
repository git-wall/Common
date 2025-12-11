package org.app.common.performance;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.DoubleCursor;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.LongCursor;
import com.carrotsearch.hppc.procedures.*;

import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Utility class for working with High Performance Primitive Collections (HPPC).
 * This class provides easy-to-use methods for common operations on HPPC collections.
 */
public class HPPC {

    // ==================== IntArrayList utilities ====================

    /**
     * Creates a new IntArrayList from the given array.
     *
     * @param array the array to create the list from
     * @return a new IntArrayList containing the elements from the array
     */
    public static IntArrayList newIntList(int... array) {
        IntArrayList list = new IntArrayList(array.length);
        list.add(array);
        return list;
    }

    /**
     * Creates a new IntArrayList from the given range.
     *
     * @param start the start value (inclusive)
     * @param end the end value (exclusive)
     * @return a new IntArrayList containing the elements in the range
     */
    public static IntArrayList newIntRange(int start, int end) {
        IntArrayList list = new IntArrayList(end - start);
        for (int i = start; i < end; i++) {
            list.add(i);
        }
        return list;
    }

    /**
     * Converts an IntArrayList to a Java array.
     *
     * @param list the IntArrayList to convert
     * @return a new array containing the elements from the list
     */
    public static int[] toArray(IntArrayList list) {
        return list.toArray();
    }

    /**
     * Converts an IntArrayList to a Java List.
     *
     * @param list the IntArrayList to convert
     * @return a new List containing the elements from the list
     */
    public static List<Integer> toJavaList(IntArrayList list) {
        return IntStream.of(list.toArray()).boxed().collect(Collectors.toList());
    }

    /**
     * Applies the given consumer to each element in the IntArrayList.
     *
     * @param list the IntArrayList to iterate over
     * @param consumer the consumer to apply to each element
     */
    public static void forEach(IntArrayList list, IntConsumer consumer) {
        list.forEach((IntProcedure) consumer::accept);
    }

    /**
     * Filters the IntArrayList based on the given predicate.
     *
     * @param list the IntArrayList to filter
     * @param predicate the predicate to test each element
     * @return a new IntArrayList containing only the elements that satisfy the predicate
     */
    public static IntArrayList filter(IntArrayList list, IntPredicate predicate) {
        IntArrayList result = new IntArrayList();
        for (IntCursor cursor : list) {
            if (predicate.test(cursor.value)) {
                result.add(cursor.value);
            }
        }
        return result;
    }

    /**
     * Maps each element in the IntArrayList using the given function.
     *
     * @param list the IntArrayList to map
     * @param function the function to apply to each element
     * @return a new IntArrayList containing the mapped elements
     */
    public static IntArrayList mapToInt(IntArrayList list, IntUnaryOperator function) {
        IntArrayList result = new IntArrayList(list.size());
        for (IntCursor cursor : list) {
            result.add(function.apply(cursor.value));
        }
        return result;
    }

    /**
     * Maps each element in the IntArrayList to a double using the given function.
     *
     * @param list the IntArrayList to map
     * @param function the function to apply to each element
     * @return a new DoubleArrayList containing the mapped elements
     */
    public static DoubleArrayList mapToDouble(IntArrayList list, IntToDoubleFunction function) {
        DoubleArrayList result = new DoubleArrayList(list.size());
        for (IntCursor cursor : list) {
            result.add(function.apply(cursor.value));
        }
        return result;
    }

    /**
     * Maps each element in the IntArrayList to a long using the given function.
     *
     * @param list the IntArrayList to map
     * @param function the function to apply to each element
     * @return a new LongArrayList containing the mapped elements
     */
    public static LongArrayList mapToLong(IntArrayList list, IntToLongFunction function) {
        LongArrayList result = new LongArrayList(list.size());
        for (IntCursor cursor : list) {
            result.add(function.apply(cursor.value));
        }
        return result;
    }

    // ==================== DoubleArrayList utilities ====================

    /**
     * Creates a new DoubleArrayList from the given array.
     *
     * @param array the array to create the list from
     * @return a new DoubleArrayList containing the elements from the array
     */
    public static DoubleArrayList newDoubleList(double... array) {
        DoubleArrayList list = new DoubleArrayList(array.length);
        list.add(array);
        return list;
    }

    /**
     * Converts a DoubleArrayList to a Java array.
     *
     * @param list the DoubleArrayList to convert
     * @return a new array containing the elements from the list
     */
    public static double[] toArray(DoubleArrayList list) {
        return list.toArray();
    }

    /**
     * Converts a DoubleArrayList to a Java List.
     *
     * @param list the DoubleArrayList to convert
     * @return a new List containing the elements from the list
     */
    public static List<Double> toJavaList(DoubleArrayList list) {
        return DoubleStream.of(list.toArray()).boxed().collect(Collectors.toList());
    }

    /**
     * Applies the given consumer to each element in the DoubleArrayList.
     *
     * @param list the DoubleArrayList to iterate over
     * @param consumer the consumer to apply to each element
     */
    public static void forEach(DoubleArrayList list, DoubleConsumer consumer) {
        list.forEach((DoubleProcedure) consumer::accept);
    }

    /**
     * Filters the DoubleArrayList based on the given predicate.
     *
     * @param list the DoubleArrayList to filter
     * @param predicate the predicate to test each element
     * @return a new DoubleArrayList containing only the elements that satisfy the predicate
     */
    public static DoubleArrayList filter(DoubleArrayList list, DoublePredicate predicate) {
        DoubleArrayList result = new DoubleArrayList();
        for (DoubleCursor cursor : list) {
            if (predicate.test(cursor.value)) {
                result.add(cursor.value);
            }
        }
        return result;
    }

    /**
     * Maps each element in the DoubleArrayList using the given function.
     *
     * @param list the DoubleArrayList to map
     * @param function the function to apply to each element
     * @return a new DoubleArrayList containing the mapped elements
     */
    public static DoubleArrayList mapToDouble(DoubleArrayList list, DoubleUnaryOperator function) {
        DoubleArrayList result = new DoubleArrayList(list.size());
        for (DoubleCursor cursor : list) {
            result.add(function.apply(cursor.value));
        }
        return result;
    }

    // ==================== LongArrayList utilities ====================

    /**
     * Creates a new LongArrayList from the given array.
     *
     * @param array the array to create the list from
     * @return a new LongArrayList containing the elements from the array
     */
    public static LongArrayList newLongList(long... array) {
        LongArrayList list = new LongArrayList(array.length);
        list.add(array);
        return list;
    }

    /**
     * Converts a LongArrayList to a Java array.
     *
     * @param list the LongArrayList to convert
     * @return a new array containing the elements from the list
     */
    public static long[] toArray(LongArrayList list) {
        return list.toArray();
    }

    /**
     * Converts a LongArrayList to a Java List.
     *
     * @param list the LongArrayList to convert
     * @return a new List containing the elements from the list
     */
    public static List<Long> toJavaList(LongArrayList list) {
        return LongStream.of(list.toArray()).boxed().collect(Collectors.toList());
    }

    /**
     * Applies the given consumer to each element in the LongArrayList.
     *
     * @param list the LongArrayList to iterate over
     * @param consumer the consumer to apply to each element
     */
    public static void forEach(LongArrayList list, LongConsumer consumer) {
        list.forEach((LongProcedure) consumer::accept);
    }

    /**
     * Filters the LongArrayList based on the given predicate.
     *
     * @param list the LongArrayList to filter
     * @param predicate the predicate to test each element
     * @return a new LongArrayList containing only the elements that satisfy the predicate
     */
    public static LongArrayList filter(LongArrayList list, LongPredicate predicate) {
        LongArrayList result = new LongArrayList();
        for (LongCursor cursor : list) {
            if (predicate.test(cursor.value)) {
                result.add(cursor.value);
            }
        }
        return result;
    }

    // ==================== Map utilities ====================

    // -------------------- IntIntHashMap --------------------

    /**
     * Creates a new IntIntHashMap from the given keys and values.
     *
     * @param keys the keys for the map
     * @param values the values for the map
     * @return a new IntIntHashMap with the given keys and values
     * @throws IllegalArgumentException if keys and values have different lengths
     */
    public static IntIntHashMap newIntIntMap(int[] keys, int[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values must have the same length");
        }

        IntIntHashMap map = new IntIntHashMap(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Creates a new IntIntHashMap with a custom load factor and expected size.
     * This can improve performance by reducing the need for rehashing.
     *
     * @param loadFactor the load factor (0.0-1.0, determines how full the map can get before resizing)
     * @param expectedSize the expected number of elements
     * @return a new IntIntHashMap with the specified parameters
     */
    public static IntIntHashMap newIntIntMap(float loadFactor, int expectedSize) {
        return new IntIntHashMap(expectedSize, loadFactor);
    }

    /**
     * Gets the keys from an IntIntHashMap as an IntArrayList.
     *
     * @param map the map to get the keys from
     * @return an IntArrayList containing the keys from the map
     */
    public static IntArrayList keys(IntIntHashMap map) {
        IntArrayList keys = new IntArrayList(map.size());
        map.forEach((IntIntProcedure) (key, value) -> keys.add(key));
        return keys;
    }

    /**
     * Gets the values from an IntIntHashMap as an IntArrayList.
     *
     * @param map the map to get the values from
     * @return an IntArrayList containing the values from the map
     */
    public static IntArrayList values(IntIntHashMap map) {
        IntArrayList values = new IntArrayList(map.size());
        map.forEach((IntIntProcedure) (key, value) -> values.add(value));
        return values;
    }

    /**
     * Performs a fast lookup in the map with a default value if the key is not found.
     * This avoids the need to check for key existence separately.
     *
     * @param map the map to look up in
     * @param key the key to look up
     * @param defaultValue the default value to return if the key is not found
     * @return the value associated with the key, or the default value if the key is not found
     */
    public static int getOrDefault(IntIntHashMap map, int key, int defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Adds all entries from one map to another. This is more efficient than
     * adding entries one by one.
     *
     * @param target the target map to add entries to
     * @param source the source map to get entries from
     */
    public static void putAll(IntIntHashMap target, IntIntHashMap source) {
        target.putAll(source);
    }

    // -------------------- IntObjectHashMap --------------------

    /**
     * Creates a new IntObjectHashMap from the given keys and values.
     *
     * @param <V> the type of values in the map
     * @param keys the keys for the map
     * @param values the values for the map
     * @return a new IntObjectHashMap with the given keys and values
     * @throws IllegalArgumentException if keys and values have different lengths
     */
    public static <V> IntObjectHashMap<V> newIntObjectMap(int[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values must have the same length");
        }

        IntObjectHashMap<V> map = new IntObjectHashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Creates a new IntObjectHashMap with a custom load factor and expected size.
     * This can improve performance by reducing the need for rehashing.
     *
     * @param <V> the type of values in the map
     * @param loadFactor the load factor (0.0-1.0, determines how full the map can get before resizing)
     * @param expectedSize the expected number of elements
     * @return a new IntObjectHashMap with the specified parameters
     */
    public static <V> IntObjectHashMap<V> newIntObjectMap(float loadFactor, int expectedSize) {
        return new IntObjectHashMap<>(expectedSize, loadFactor);
    }

    /**
     * Gets the keys from an IntObjectHashMap as an IntArrayList.
     *
     * @param <V> the type of values in the map
     * @param map the map to get the keys from
     * @return an IntArrayList containing the keys from the map
     */
    public static <V> IntArrayList keys(IntObjectHashMap<V> map) {
        IntArrayList keys = new IntArrayList(map.size());
        map.forEach((IntObjectProcedure<V>) (key, value) -> keys.add(key));
        return keys;
    }

    /**
     * Performs a fast lookup in the map with a default value if the key is not found.
     * This avoids the need to check for key existence separately.
     *
     * @param <V> the type of values in the map
     * @param map the map to look up in
     * @param key the key to look up
     * @param defaultValue the default value to return if the key is not found
     * @return the value associated with the key, or the default value if the key is not found
     */
    public static <V> V getOrDefault(IntObjectHashMap<V> map, int key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    // -------------------- LongLongHashMap --------------------

    /**
     * Creates a new LongLongHashMap from the given keys and values.
     *
     * @param keys the keys for the map
     * @param values the values for the map
     * @return a new LongLongHashMap with the given keys and values
     * @throws IllegalArgumentException if keys and values have different lengths
     */
    public static LongLongHashMap newLongLongMap(long[] keys, long[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values must have the same length");
        }

        LongLongHashMap map = new LongLongHashMap(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Creates a new LongLongHashMap with a custom load factor and expected size.
     * This can improve performance by reducing the need for rehashing.
     *
     * @param loadFactor the load factor (0.0-1.0, determines how full the map can get before resizing)
     * @param expectedSize the expected number of elements
     * @return a new LongLongHashMap with the specified parameters
     */
    public static LongLongHashMap newLongLongMap(float loadFactor, int expectedSize) {
        return new LongLongHashMap(expectedSize, loadFactor);
    }

    /**
     * Gets the keys from a LongLongHashMap as a LongArrayList.
     *
     * @param map the map to get the keys from
     * @return a LongArrayList containing the keys from the map
     */
    public static LongArrayList keys(LongLongHashMap map) {
        LongArrayList keys = new LongArrayList(map.size());
        map.forEach((LongLongProcedure) (key, value) -> keys.add(key));
        return keys;
    }

    /**
     * Gets the values from a LongLongHashMap as a LongArrayList.
     *
     * @param map the map to get the values from
     * @return a LongArrayList containing the values from the map
     */
    public static LongArrayList values(LongLongHashMap map) {
        LongArrayList values = new LongArrayList(map.size());
        map.forEach((LongLongProcedure) (key, value) -> values.add(value));
        return values;
    }

    /**
     * Performs a fast lookup in the map with a default value if the key is not found.
     * This avoids the need to check for key existence separately.
     *
     * @param map the map to look up in
     * @param key the key to look up
     * @param defaultValue the default value to return if the key is not found
     * @return the value associated with the key, or the default value if the key is not found
     */
    public static long getOrDefault(LongLongHashMap map, long key, long defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    // -------------------- LongObjectHashMap --------------------

    /**
     * Creates a new LongObjectHashMap from the given keys and values.
     *
     * @param <V> the type of values in the map
     * @param keys the keys for the map
     * @param values the values for the map
     * @return a new LongObjectHashMap with the given keys and values
     * @throws IllegalArgumentException if keys and values have different lengths
     */
    public static <V> LongObjectHashMap<V> newLongObjectMap(long[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values must have the same length");
        }

        LongObjectHashMap<V> map = new LongObjectHashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Creates a new LongObjectHashMap with a custom load factor and expected size.
     * This can improve performance by reducing the need for rehashing.
     *
     * @param <V> the type of values in the map
     * @param loadFactor the load factor (0.0-1.0, determines how full the map can get before resizing)
     * @param expectedSize the expected number of elements
     * @return a new LongObjectHashMap with the specified parameters
     */
    public static <V> LongObjectHashMap<V> newLongObjectMap(float loadFactor, int expectedSize) {
        return new LongObjectHashMap<>(expectedSize, loadFactor);
    }

    /**
     * Gets the keys from a LongObjectHashMap as a LongArrayList.
     *
     * @param <V> the type of values in the map
     * @param map the map to get the keys from
     * @return a LongArrayList containing the keys from the map
     */
    public static <V> LongArrayList keys(LongObjectHashMap<V> map) {
        LongArrayList keys = new LongArrayList(map.size());
        map.forEach((LongObjectProcedure<V>) (key, value) -> keys.add(key));
        return keys;
    }

    /**
     * Performs a fast lookup in the map with a default value if the key is not found.
     * This avoids the need to check for key existence separately.
     *
     * @param <V> the type of values in the map
     * @param map the map to look up in
     * @param key the key to look up
     * @param defaultValue the default value to return if the key is not found
     * @return the value associated with the key, or the default value if the key is not found
     */
    public static <V> V getOrDefault(LongObjectHashMap<V> map, long key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    // -------------------- ObjectObjectHashMap --------------------

    /**
     * Creates a new ObjectObjectHashMap from the given keys and values.
     *
     * @param <K> the type of keys in the map
     * @param <V> the type of values in the map
     * @param keys the keys for the map
     * @param values the values for the map
     * @return a new ObjectObjectHashMap with the given keys and values
     * @throws IllegalArgumentException if keys and values have different lengths
     */
    public static <K, V> ObjectObjectHashMap<K, V> newObjectObjectMap(K[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values must have the same length");
        }

        ObjectObjectHashMap<K, V> map = new ObjectObjectHashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Creates a new ObjectObjectHashMap with a custom load factor and expected size.
     * This can improve performance by reducing the need for rehashing.
     *
     * @param <K> the type of keys in the map
     * @param <V> the type of values in the map
     * @param loadFactor the load factor (0.0-1.0, determines how full the map can get before resizing)
     * @param expectedSize the expected number of elements
     * @return a new ObjectObjectHashMap with the specified parameters
     */
    public static <K, V> ObjectObjectHashMap<K, V> newObjectObjectMap(float loadFactor, int expectedSize) {
        return new ObjectObjectHashMap<>(expectedSize, loadFactor);
    }

    /**
     * Performs a fast lookup in the map with a default value if the key is not found.
     * This avoids the need to check for key existence separately.
     *
     * @param <K> the type of keys in the map
     * @param <V> the type of values in the map
     * @param map the map to look up in
     * @param key the key to look up
     * @param defaultValue the default value to return if the key is not found
     * @return the value associated with the key, or the default value if the key is not found
     */
    public static <K, V> V getOrDefault(ObjectObjectHashMap<K, V> map, K key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    // -------------------- IntLongHashMap --------------------

    /**
     * Creates a new IntLongHashMap from the given keys and values.
     *
     * @param keys the keys for the map
     * @param values the values for the map
     * @return a new IntLongHashMap with the given keys and values
     * @throws IllegalArgumentException if keys and values have different lengths
     */
    public static IntLongHashMap newIntLongMap(int[] keys, long[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values must have the same length");
        }

        IntLongHashMap map = new IntLongHashMap(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Creates a new IntLongHashMap with a custom load factor and expected size.
     * This can improve performance by reducing the need for rehashing.
     *
     * @param loadFactor the load factor (0.0-1.0, determines how full the map can get before resizing)
     * @param expectedSize the expected number of elements
     * @return a new IntLongHashMap with the specified parameters
     */
    public static IntLongHashMap newIntLongMap(float loadFactor, int expectedSize) {
        return new IntLongHashMap(expectedSize, loadFactor);
    }

    /**
     * Gets the keys from an IntLongHashMap as an IntArrayList.
     *
     * @param map the map to get the keys from
     * @return an IntArrayList containing the keys from the map
     */
    public static IntArrayList keys(IntLongHashMap map) {
        IntArrayList keys = new IntArrayList(map.size());
        map.forEach((IntLongProcedure) (key, value) -> keys.add(key));
        return keys;
    }

    /**
     * Gets the values from an IntLongHashMap as a LongArrayList.
     *
     * @param map the map to get the values from
     * @return a LongArrayList containing the values from the map
     */
    public static LongArrayList values(IntLongHashMap map) {
        LongArrayList values = new LongArrayList(map.size());
        map.forEach((IntLongProcedure) (key, value) -> values.add(value));
        return values;
    }

    /**
     * Performs a fast lookup in the map with a default value if the key is not found.
     * This avoids the need to check for key existence separately.
     *
     * @param map the map to look up in
     * @param key the key to look up
     * @param defaultValue the default value to return if the key is not found
     * @return the value associated with the key, or the default value if the key is not found
     */
    public static long getOrDefault(IntLongHashMap map, int key, long defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    // -------------------- LongIntHashMap --------------------

    /**
     * Creates a new LongIntHashMap from the given keys and values.
     *
     * @param keys the keys for the map
     * @param values the values for the map
     * @return a new LongIntHashMap with the given keys and values
     * @throws IllegalArgumentException if keys and values have different lengths
     */
    public static LongIntHashMap newLongIntMap(long[] keys, int[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values must have the same length");
        }

        LongIntHashMap map = new LongIntHashMap(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    /**
     * Creates a new LongIntHashMap with a custom load factor and expected size.
     * This can improve performance by reducing the need for rehashing.
     *
     * @param loadFactor the load factor (0.0-1.0, determines how full the map can get before resizing)
     * @param expectedSize the expected number of elements
     * @return a new LongIntHashMap with the specified parameters
     */
    public static LongIntHashMap newLongIntMap(float loadFactor, int expectedSize) {
        return new LongIntHashMap(expectedSize, loadFactor);
    }

    /**
     * Gets the keys from a LongIntHashMap as a LongArrayList.
     *
     * @param map the map to get the keys from
     * @return a LongArrayList containing the keys from the map
     */
    public static LongArrayList keys(LongIntHashMap map) {
        LongArrayList keys = new LongArrayList(map.size());
        map.forEach((LongIntProcedure) (key, value) -> keys.add(key));
        return keys;
    }

    /**
     * Gets the values from a LongIntHashMap as an IntArrayList.
     *
     * @param map the map to get the values from
     * @return an IntArrayList containing the values from the map
     */
    public static IntArrayList values(LongIntHashMap map) {
        IntArrayList values = new IntArrayList(map.size());
        map.forEach((LongIntProcedure) (key, value) -> values.add(value));
        return values;
    }

    /**
     * Performs a fast lookup in the map with a default value if the key is not found.
     * This avoids the need to check for key existence separately.
     *
     * @param map the map to look up in
     * @param key the key to look up
     * @param defaultValue the default value to return if the key is not found
     * @return the value associated with the key, or the default value if the key is not found
     */
    public static int getOrDefault(LongIntHashMap map, long key, int defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }


    // ==================== Set utilities ====================

    /**
     * Creates a new IntHashSet from the given array.
     *
     * @param array the array to create the set from
     * @return a new IntHashSet containing the elements from the array
     */
    public static IntHashSet newIntSet(int... array) {
        IntHashSet set = new IntHashSet(array.length);
        for (int value : array) {
            set.add(value);
        }
        return set;
    }

    /**
     * Creates a new LongHashSet from the given array.
     *
     * @param array the array to create the set from
     * @return a new LongHashSet containing the elements from the array
     */
    public static LongHashSet newLongSet(long... array) {
        LongHashSet set = new LongHashSet(array.length);
        for (long value : array) {
            set.add(value);
        }
        return set;
    }

    /**
     * Converts an IntHashSet to a Java array.
     *
     * @param set the IntHashSet to convert
     * @return a new array containing the elements from the set
     */
    public static int[] toArray(IntHashSet set) {
        int[] array = new int[set.size()];
        int index = 0;
        for (IntCursor cursor : set) {
            array[index++] = cursor.value;
        }
        return array;
    }

    /**
     * Converts a LongHashSet to a Java array.
     *
     * @param set the LongHashSet to convert
     * @return a new array containing the elements from the set
     */
    public static long[] toArray(LongHashSet set) {
        long[] array = new long[set.size()];
        int index = 0;
        for (LongCursor cursor : set) {
            array[index++] = cursor.value;
        }
        return array;
    }

    /**
     * Functional interface for int to int mapping operations.
     */
    @FunctionalInterface
    public interface IntUnaryOperator {
        int apply(int operand);
    }

    /**
     * Functional interface for int to double mapping operations.
     */
    @FunctionalInterface
    public interface IntToDoubleFunction {
        double apply(int value);
    }

    /**
     * Functional interface for int to long mapping operations.
     */
    @FunctionalInterface
    public interface IntToLongFunction {
        long apply(int value);
    }

    /**
     * Functional interface for double to double mapping operations.
     */
    @FunctionalInterface
    public interface DoubleUnaryOperator {
        double apply(double operand);
    }
}
