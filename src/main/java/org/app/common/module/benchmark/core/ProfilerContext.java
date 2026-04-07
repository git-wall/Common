package org.app.common.module.benchmark.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ProfilerContext {

    private static final ThreadLocal<Deque<TraceNode>> STACK =
        ThreadLocal.withInitial(ArrayDeque::new);

    private static final ThreadLocal<TraceNode> ROOT = new ThreadLocal<>();

    public static void enter(String methodFullName) {
        TraceNode node = new TraceNode(methodFullName);
        node.startNs = System.nanoTime();
        node.startMemKb = usedMemKb();

        Deque<TraceNode> stack = STACK.get();

        if (stack.isEmpty()) {
            ROOT.set(node);
        } else {
            TraceNode parent = stack.peek();
            node.parent = parent;

            if (parent.childrenMap.containsKey(methodFullName)) {
                node = parent.childrenMap.get(methodFullName);
            } else {
                parent.childrenMap.put(methodFullName, node);
                parent.children.add(node);
            }
        }

        stack.push(node);
    }

    public static void exit() {
        Deque<TraceNode> stack = STACK.get();
        if (stack.isEmpty()) {
            return;
        }

        TraceNode node = stack.pop();

        long timeMs = (System.nanoTime() - node.startNs) / 1_000_000;
        long memKb = usedMemKb() - node.startMemKb;

        if (node.callCount == 1) {
            node.timeMs = timeMs;
            node.memKb = memKb;
        } else {
            node.mergeCall(timeMs, memKb);
        }
    }

    private static long usedMemKb() {
        Runtime rt = Runtime.getRuntime();
        System.gc();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        return (total - free) / 1024;
    }

    public static TraceNode finish() {
        Deque<TraceNode> stack = STACK.get();

        while (!stack.isEmpty()) {
            exit();
        }

        TraceNode root = ROOT.get();

        STACK.remove();
        ROOT.remove();

        return root;
    }

    public static boolean isActive() {
        return !STACK.get().isEmpty();
    }
}
