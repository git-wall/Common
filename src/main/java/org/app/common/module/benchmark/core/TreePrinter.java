package org.app.common.module.benchmark.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.benchmark.annotation.ApiProfiled;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TreePrinter {

    public static void print(TraceNode root, ApiProfiled api) {
        if (root == null) {
            return;
        }

        log.info("=== Profile Result: {} ===", root.method);
        printNode(root, "", true, api);
        log.info("===============================");
    }

    private static void printNode(
        TraceNode node,
        String prefix,
        boolean isLast,
        ApiProfiled api
    ) {
        StringBuilder warn = new StringBuilder();
        if (node.timeMs > api.warnTimeMs()) {
            warn.append(" ⏱SLOW");
        }
        if (node.memKb > api.warnMemKb()) {
            warn.append(" 💾HIGH_MEM");
        }

        String branch = isLast ? "└─ " : "├─ ";

        String callInfo = node.callCount > 1 ? " [×" + node.callCount + "]" : "";

        log.info(
            "{}{}{} ({} ms, {} KB){}{}",
            prefix,
            branch,
            node.method,
            node.timeMs,
            node.memKb,
            callInfo,
            warn
        );

        String childPrefix = prefix + (isLast ? "   " : "│  ");

        for (int i = 0; i < node.children.size(); i++) {
            boolean isLastChild = (i == node.children.size() - 1);
            printNode(node.children.get(i), childPrefix, isLastChild, api);
        }
    }
}
