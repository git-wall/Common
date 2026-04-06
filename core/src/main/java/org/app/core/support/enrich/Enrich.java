package org.app.core.support.enrich;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.app.core.support.Loop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class providing generic enrichment functions for objects and collections.
 * Allows enriching single objects, list elements, and other collection types.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Enrich {

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
                SINGLE OBJECT
    \*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/

    /**
     * Enriches a single object with additional data.
     *
     * @param obj      The object to enrich
     * @param enricher Function that takes the object and returns an enriched version
     * @param <T>      Type of the object
     * @return Enriched object
     */
    public static <T> T object(T obj, Function<T, T> enricher) {
        if (obj == null || enricher == null) {
            return obj;
        }
        return enricher.apply(obj);
    }

    public static <T> void object(T obj, Consumer<T> consumer) {
        consumer.accept(obj);
    }

    /**
     * Enriches a single object with additional data from a context.
     *
     * @param obj      The object to enrich
     * @param context  Additional data used for enrichment
     * @param enricher BiFunction that takes the object and context and returns an enriched version
     * @param <T>      Type of the object
     * @param <C>      Type of the context
     * @return Enriched object
     */
    public static <T, C> T objectWithContext(T obj, C context, BiFunction<T, C, T> enricher) {
        if (obj == null || enricher == null) {
            return obj;
        }
        return enricher.apply(obj, context);
    }

    /**
     * Modifies a single object in-place with additional data.
     *
     * @param obj      The object to enrich
     * @param enricher Consumer that modifies the object
     * @param <T>      Type of the object
     * @return The same object after enrichment
     */
    public static <T> T objectInPlace(T obj, BiConsumer<T, T> enricher) {
        if (obj == null || enricher == null) {
            return obj;
        }
        enricher.accept(obj, obj);
        return obj;
    }

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
                COLLECTIONS
    \*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/

    /**
     * Enriches each element in a list.
     *
     * @param list     The list of objects to enrich
     * @param enricher Function that takes an object and returns an enriched version
     * @param <T>      Type of the objects in the list
     * @return List of enriched objects
     */
    public static <T> List<T> list(List<T> list, Consumer<T> enricher) {
        if (list == null || list.isEmpty() || enricher == null) {
            return list;
        }

        Loop.fill(list, enricher);
        return list;
    }

    /**
     * Enriches each element in a list with a context.
     *
     * @param list     The list of objects to enrich
     * @param context  Additional data used for enrichment
     * @param enricher BiConsumer that takes an object and context and modifies the object
     * @param <T>      Type of the objects in the list
     * @param <C>      Type of the context
     * @return List of enriched objects
     */
    public static <T, C> List<T> listWithContext(List<T> list, C context, BiConsumer<T, C> enricher) {
        if (list == null || list.isEmpty() || enricher == null) {
            return list;
        }

        Loop.fillWithContext(list, context, enricher);
        return list;
    }

    /**
     * Enriches each element in a collection.
     *
     * @param collection The collection of objects to enrich
     * @param enricher   Function that takes an object and returns an enriched version
     * @param <T>        Type of the objects in the collection
     * @param <C>        Type of the collection
     * @return Collection of enriched objects
     */
    public static <T, C extends Collection<T>> C collection(C collection, Function<T, T> enricher) {
        if (collection == null || collection.isEmpty() || enricher == null) {
            return collection;
        }

        List<T> enriched = new ArrayList<>(collection.size());
        for (T item : collection) {
            enriched.add(item != null ? enricher.apply(item) : null);
        }

        collection.clear();
        collection.addAll(enriched);
        return collection;
    }

    /**
     * Enriches values in a map.
     *
     * @param map      The map with values to enrich
     * @param enricher Function that takes a value and returns an enriched version
     * @param <K>      Type of the keys in the map
     * @param <V>      Type of the values in the map
     * @param <M>      Type of the map
     * @return Map with enriched values
     */
    public static <K, V, M extends Map<K, V>> M mapValues(M map, Function<V, V> enricher) {
        if (map == null || map.isEmpty() || enricher == null) {
            return map;
        }

        for (Map.Entry<K, V> entry : map.entrySet()) {
            V value = entry.getValue();
            if (value != null) {
                entry.setValue(enricher.apply(value));
            }
        }
        return map;
    }
}
