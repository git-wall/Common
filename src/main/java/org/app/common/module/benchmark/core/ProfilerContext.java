package org.app.common.module.benchmark.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ProfilerContext {

    private static final ThreadLocal<Deque<TraceNode>> STACK =
        ThreadLocal.withInitial(ArrayDeque::new);

    private static final ThreadLocal<TraceNode> ROOT = new ThreadLocal<>();

    public static void enter(String method) {
        TraceNode node = new TraceNode(method);
        node.startNs = System.nanoTime();
        node.startMemKb = usedMemKb();

        Deque<TraceNode> stack = STACK.get();

        if (stack.isEmpty()) {
            ROOT.set(node);
        } else {
            TraceNode parent = stack.peek();
            node.parent = parent;
            parent.children.add(node);
        }

        stack.push(node);
    }

    public static void exit() {
        Deque<TraceNode> stack = STACK.get();
        if (stack.isEmpty()) {
            return;
        }

        TraceNode node = stack.pop();
        node.timeMs = (System.nanoTime() - node.startNs) / 1_000_000;
        node.memKb = usedMemKb() - node.startMemKb;
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
