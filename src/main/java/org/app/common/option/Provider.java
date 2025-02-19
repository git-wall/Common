package org.app.common.option;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Provider {
    private Provider() {
    }

    // Collection pipeline
    /**
     * Make in function
     *
     * <pre>{@code
     * List<Benefit> benefits = benefitService.getBenefitsById(ids);
     * Map<Long, List<Benefit>> benefitsByCampaign = groupBy(benefits, Benefit::getCampaignId);
     * }</pre>
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T, K> transfer) {
        return list.stream().collect(Collectors.groupingBy(transfer));
    }

    public static <T, R> String innerJoin(List<T> list, Function<T, R> mapper, CharSequence delimiter) {
        return list.stream()
                .map(mapper.andThen(Object::toString))
                .collect(Collectors.joining(delimiter));
    }

    public static <T> List<T> innerFilter(List<T> list, Predicate<T> filter) {
        if (list.isEmpty()) return Collections.emptyList();
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    public static <T, R> List<R> innerMapper(List<T> list, Function<T, R> mapper) {
        if (list.isEmpty()) return Collections.emptyList();
        return list.stream().map(mapper).collect(Collectors.toList());
    }

    // pipeline

    public static <T, R> R mapper(T t, Function<T, R> mapper) {
        return Optional.of(t).map(mapper).orElseThrow(() -> new IllegalStateException("Error when mapper"));
    }

    public static <T> T filterThrow(T t, Predicate<T> filter, String error) {
        return Optional.ofNullable(t)
                .filter(filter)
                .orElseThrow(() -> new IllegalStateException(error));
    }

    public static <T> T filterAndElse(T t, Predicate<T> filter, T other) {
        return Optional.ofNullable(t)
                .filter(filter)
                .orElse(other);
    }

    // combine

    public static <T> T[] combine(T[] src1, T[] src2) {
        Object[] combineArray = new Object[src1.length + src2.length];
        System.arraycopy(src1, 0, combineArray, 0, src1.length);
        System.arraycopy(src2, 0, combineArray, src1.length, src2.length);
        return (T[]) combineArray;
    }
}
