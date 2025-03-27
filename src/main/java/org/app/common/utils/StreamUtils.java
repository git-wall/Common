package org.app.common.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.function.Failable;
import org.thymeleaf.util.ListUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtils {
    public static final int STREAM_MAX_SIZE = 10000;

    public static <T> Stream<T> of(Collection<T> collection) {
        return collection.size() <= STREAM_MAX_SIZE ? collection.stream() : collection.parallelStream();
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
                .map(mapper.andThen(Object::toString))
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

    public static <T, R> List<R> innerActionAssert(Collection<T> list, Predicate<T> filter, Function<T, R> mapper) {
        return list.stream().filter(filter).map(mapper).collect(Collectors.toList());
    }

    public static <T> List<T> failStream(Collection<T> list, Method m, Object... args) {
        return Failable.stream(list.stream())
                .map((o) -> (T) m.invoke(o, args))
                .collect(Collectors.toList());
    }
}
