package org.app.common.module.benchmark.export;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.app.common.module.benchmark.aggregate.AggNode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JacksonMapper {

    public static ProfileJsonNode toJson(AggNode agg, Mode mode) {
        ProfileJsonNode json = new ProfileJsonNode();
        json.method = agg.method;
        json.count = agg.stat.count;

        switch (mode) {
            case AVG:
                json.timeMs = Math.round(agg.stat.avgTime);
                json.memoryKb = Math.round(agg.stat.avgMem);
                break;

            case MIN:
                json.timeMs = agg.stat.minTime;
                json.memoryKb = agg.stat.minMem;
                break;

            case MAX:
                json.timeMs = agg.stat.maxTime;
                json.memoryKb = agg.stat.maxMem;
                break;
        }

        for (AggNode child : agg.children.values()) {
            json.children.add(toJson(child, mode));
        }

        return json;
    }
}
