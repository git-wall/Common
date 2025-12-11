package org.app.common.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.function.Failable;
import org.app.common.provider.Provider;
import org.thymeleaf.util.ListUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamUtils {
    private StreamUtils() {}

    public static final int STREAM_MAX_SIZE;
    public static final int STREAM_MIN_SIZE = 50_000;

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
        if (ListUtils.isEmpty(list)) return Collections.emptyMap();
        return list.stream().collect(Collectors.groupingBy(transfer));
    }

    public static <T, R> String innerJoin(Collection<T> list, Function<T, R> mapper, CharSequence delimiter) {
        if (CollectionUtils.isEmpty(list)) return "";
        return list.stream()
            .map(Provider.thenToString(mapper))
            .collect(Collectors.joining(delimiter));
    }

    public static <T> List<T> innerFilter(Collection<T> list, Predicate<T> filter) {
        if (CollectionUtils.isEmpty(list)) return Collections.emptyList();
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    public static <T, R> List<R> innerMapper(Collection<T> list, Function<T, R> mapper) {
        if (CollectionUtils.isEmpty(list)) return Collections.emptyList();
        return list.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T, R> List<R> mapAndFilter(Collection<T> list, Predicate<T> filter, Function<T, R> mapper) {
        return list.stream().filter(filter).map(mapper).collect(Collectors.toList());
    }

    public static <T> List<T> failStream(Collection<T> list, Method m, Object... args) {
        return Failable.stream(list.stream())
            .map((o) -> (T) m.invoke(o, args))
            .collect(Collectors.toList());
    }

    public static <K, V> Map<K, V> asMap(Collection<V> list, Function<V, K> mapper) {
        if (CollectionUtils.isEmpty(list)) return Collections.emptyMap();
        return list.stream().collect(Collectors.toMap(mapper, Function.identity()));
    }

    static {
        STREAM_MAX_SIZE = benchmark();
        System.out.printf("Calibrated threshold: STREAM<%d", STREAM_MAX_SIZE);
    }

    private static int benchmark() {
        List<Integer> sample = IntStream.range(0, 200_000).boxed().collect(Collectors.toList());

        ToIntFunction<Integer> worker = x -> {
            double r = 0;
            for (int i = 0; i < 50; i++) r += Math.sqrt(x + i);
            return (int) r;
        };

        int step = 5_000;
        for (int size = step; size <= sample.size(); size += step) {
            List<Integer> sub = sample.subList(0, size);

            double tStream = time(() -> sub.stream().mapToInt(worker).sum());
            double tParallel = time(() -> sub.parallelStream().mapToInt(worker).sum());

            if (tParallel < tStream) {
                return size;
            }
        }
        return sample.size(); // fallback if parallel never wins
    }

    private static double time(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return (System.nanoTime() - start) / 1_000_000.0;
    }
}
