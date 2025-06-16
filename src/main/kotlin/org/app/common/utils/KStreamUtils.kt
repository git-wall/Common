package org.app.common.utils

import org.thymeleaf.util.ListUtils
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream

object KStreamUtils {

    private const val STREAM_MAX_SIZE: Int = 10000

    fun <T> of(collection: Collection<T>): Stream<T> {
        return if (collection.size <= STREAM_MAX_SIZE) collection.stream() else collection.parallelStream()
    }

    fun <T, K> group(collection: Collection<T>, mapper: (T) -> K): Map<K, List<T>> {
        if (collection.isEmpty()) return emptyMap()
        return collection.groupBy(mapper)
    }

    fun <T, K> groupBy(collection: Collection<T>, mapper: (T) -> K): Map<K, List<T>> {
        if (collection.isEmpty()) return emptyMap()
        return collection.stream()
            .collect(Collectors.groupingBy(mapper))
    }

    fun <T, K> groupBy(list: List<T?>, transfer: Function<T?, K>): Map<K, List<T?>> {
        if (ListUtils.isEmpty(list)) return emptyMap()
        return list.stream().collect(Collectors.groupingBy(transfer))
    }

    fun <T> filter(list: List<T>, filter: (T) -> Boolean): List<T> {
        if (ListUtils.isEmpty(list)) return emptyList()
        return list.filter { filter(it) }
    }

    fun <T> filter(list: List<T>, filter: Predicate<T>): List<T> {
        if (ListUtils.isEmpty(list)) return emptyList()
        return list.filter { filter.test(it) }
    }

    fun <T, R> mapper(list: List<T>, mapper: (T) -> R): List<R> {
        if (ListUtils.isEmpty(list)) return emptyList()
        return list.map(mapper)
    }

    fun <T, R> mapper(list: List<T>, mapper: Function<T, R>): List<R> {
        if (ListUtils.isEmpty(list)) return emptyList()
        return list.map { mapper.apply(it) }
    }

    fun <T, R> clean(list: List<T>, filter: (T) -> Boolean, mapper: (T) -> R): List<R> {
        if (ListUtils.isEmpty(list)) return emptyList()
        return list.filter(filter).map(mapper)
    }

    fun <T, R> clean(list: List<T>, filter: Predicate<T>, mapper: Function<T, R>): List<R> {
        if (ListUtils.isEmpty(list)) return emptyList()
        return list.filter { filter.test(it) }.map { mapper.apply(it) }
    }
}