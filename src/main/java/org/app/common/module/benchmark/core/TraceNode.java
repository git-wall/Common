package org.app.common.module.benchmark.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TraceNode {

    public final String method;

    long startNs;
    long startMemKb;

    public long timeMs;
    public long memKb;

    public int callCount = 1;

    public final Map<String, TraceNode> childrenMap = new LinkedHashMap<>();
    public final List<TraceNode> children = new ArrayList<>();
    public TraceNode parent;

    public TraceNode(String method) {
        this.method = method;
    }

    public void mergeCall(long timeMs, long memKb) {
        this.callCount++;
        this.timeMs += timeMs;
        this.memKb += memKb;
    }

    @Override
    public String toString() {
        return String.format("%s [%dms, %dKB, calls=%d]", method, timeMs, memKb, callCount);
    }
}
