package org.app.common.module.benchmark.aggregate;

import java.util.LinkedHashMap;
import java.util.Map;

public class AggNode {

    public String method;

    public AggStat stat = new AggStat();

    public Map<String, AggNode> children = new LinkedHashMap<>();

    @Override
    public String toString() {
        return String.format("%s [%s]", method, stat);
    }
}
