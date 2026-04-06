package org.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.app.core.support.Provider;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// can extend to develop more methods
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class StreamUtils {
    public static final int STREAM_MAX_SIZE = 50_000;
    public static final int STREAM_MIN_SIZE = 5_000;

    public static <T> Stream<T> of(Collection<T> collection) {
        return collection.size() < STREAM_MIN_SIZE ? collection.stream() : collection.parallelStream();
    }

    public static <T> Stream<T> ofThreadHold(Collection<T> collection) {
        return collection.size() < STREAM_MAX_SIZE ? collection.stream() : collection.parallelStream();
    }

    /**
     * Make in function
     *
     * <pre>{@code
     * List<Benefit> benefits = benefitService.getBenefitsById(ids);
     * Map<Long, List<Benefit>> benefitsByCampaign = groupBy(benefits, Benefit::getCampaignId);
     * }</pre>
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T, K> transfer) {
        if (DataUtils.isEmpty(list)) return Collections.emptyMap();
        return list.stream().collect(Collectors.groupingBy(transfer));
    }

    public static <T, R> String join(Collection<T> list, Function<T, R> mapper, CharSequence delimiter) {
        if (DataUtils.isEmpty(list)) return "";
        return list.stream()
            .map(Provider.thenToString(mapper))
            .collect(Collectors.joining(delimiter));
    }

    public static <T> List<T> filter(Collection<T> list, Predicate<T> filter) {
        if (DataUtils.isEmpty(list)) return Collections.emptyList();
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    public static <T, R> List<R> map(Collection<T> list, Function<T, R> mapper) {
        if (DataUtils.isEmpty(list)) return Collections.emptyList();
        return list.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T, R> List<R> mapAndFilter(Collection<T> list, Predicate<T> filter, Function<T, R> mapper) {
        return list.stream().filter(filter).map(mapper).collect(Collectors.toList());
    }

    public static <T, R> String flatMapJoin(
        Collection<T> source,
        Function<T, ? extends Collection<R>> flatMapper,
        Function<R, String> mapper,
        String delimiter
    ) {
        return source.stream()
            .filter(Provider.isNotNull())
            .flatMap(t -> flatMapper.apply(t).stream())
            .map(Provider.thenToString(mapper))
            .collect(Collectors.joining(delimiter));
    }

    public static <K, V> Map<K, V> filterThentoMap(Collection<V> list, Predicate<V> filter, Function<V, K> keyMapper) {
        if (DataUtils.isEmpty(list)) return Collections.emptyMap();
        return list.stream()
            .filter(filter)
            .collect(Collectors.toMap(
                keyMapper,
                Function.identity(), (a, b) -> a,
                () -> new HashMap<>((int) (list.size() / 0.75f) + 1))
            );
    }

    public static <K, V> Map<K, V> toMap(Collection<V> list, Function<V, K> keyMapper) {
        if (DataUtils.isEmpty(list)) return Collections.emptyMap();
        return list.stream().collect(Collectors.toMap(
            keyMapper,
            Function.identity(), (a, b) -> a,
            () -> new HashMap<>((int) (list.size() / 0.75f) + 1))
        );
    }
}
