package org.app.core.support.logic;

import org.app.common.support.Loop;
import org.app.common.utils.StreamUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Logic {
    public static <T, N, R> List<R> getChildList(Collection<T> list,
                                                 Function<T, N> extractor,
                                                 Function<List<N>, List<R>> processor) {
        List<N> extracted = Loop.map(list, extractor);
        return processor.apply(extracted);
    }

    public static <T, N, R, I> Map<I, List<R>> getChildMap(Collection<T> parent,
                                                           Function<T, N> extractor,
                                                           Function<List<N>, List<R>> processor,
                                                           Function<R, I> idMapper) {
        // extract ids from parent
        List<N> ids = Loop.map(parent, extractor);
        // process ids to get child list
        List<R> child = processor.apply(ids);
        // group child by ids
        return StreamUtils.groupBy(child, idMapper);
    }

    public static <T, N, R> List<T> fill(
        Collection<T> parent,
        Function<T, N> extractorIds,
        Function<List<N>, List<R>> processor,
        Function<R, N> idMapper,
        BiFunction<T, List<R>, T> fill) {

        Map<N, List<R>> chidMap = getChildMap(parent, extractorIds, processor, idMapper);
        // fill child to parent
        return parent.stream()
            .map(e -> {
                List<R> c = chidMap.get(extractorIds.apply(e));
                return fill.apply(e, c);
            })
            .collect(Collectors.toList());
    }

    public static <T, N, R, O> List<O> fillTo(
        Collection<T> parent,
        Function<T, N> extractorIds,
        Function<List<N>, List<R>> processor,
        Function<R, N> idMapper,
        BiFunction<T, List<R>, O> fill) {

        Map<N, List<R>> chidMap = getChildMap(parent, extractorIds, processor, idMapper);
        // fill child to parent
        return parent.stream()
            .map(e -> {
                List<R> c = chidMap.get(extractorIds.apply(e));
                return fill.apply(e, c);
            })
            .collect(Collectors.toList());
    }
}
