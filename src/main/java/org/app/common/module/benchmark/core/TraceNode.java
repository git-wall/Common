package org.app.common.module.benchmark.core;

import java.util.ArrayList;
import java.util.List;

public class TraceNode {
    public final String method;

    long startNs;
    long startMemKb;

    public long timeMs;
    public long memKb;

    public final List<TraceNode> children = new ArrayList<>();
    public TraceNode parent;

    public TraceNode(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return String.format("%s [%dms, %dKB]", method, timeMs, memKb);
    }
}
