package org.app.common.usecase.enrich;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class EnrichPipeline<P, ID> {

    private final List<P> parents;
    private final Function<P, ID> parentKey;

    private EnrichPipeline(List<P> parents, Function<P, ID> parentKey) {
        this.parents = parents;
        this.parentKey = parentKey;
    }

    public static <P, ID> EnrichPipeline<P, ID> from(List<P> parents, Function<P, ID> parentKey) {
        return new EnrichPipeline<>(parents, parentKey);
    }

    public <C> EnrichPipeline<P, ID> collect(Loader<P, ID, C> collector) {
        List<ID> ids = parents.stream()
            .map(parentKey)
            .distinct()
            .collect(Collectors.toList());

        List<C> children = collector.loader.apply(ids);
        if (children.isEmpty()) return this;
        if (collector.childKey == null || collector.attacher == null) return this;

        Map<ID, List<C>> grouped = children.stream().collect(Collectors.groupingBy(collector.childKey));
        for (P parent : parents) {
            List<C> cs = grouped.get(parentKey.apply(parent));
            if (cs != null) {
                collector.attacher.accept(parent, cs);
            }
        }
        return this;
    }

    public List<P> done() {
        return parents;
    }
}
