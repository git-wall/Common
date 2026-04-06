package org.app.common.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StackTracePrinter {

    public static String printApplicationStack(Throwable t, String basePackage, int maxDepth) {
        if (t == null) return "";

        StringBuilder sb = new StringBuilder();

        sb.append(">> ");
        sb.append(t.getClass().getSimpleName());
        sb.append(": ");
        sb.append(t.getMessage());
        sb.append("\n");

        int count = 0;

        for (StackTraceElement e : t.getStackTrace()) {
            if (!e.getClassName().startsWith(basePackage)) {
                continue;
            }

            sb.append(e.getClassName())
                .append(".")
                .append(e.getMethodName())
                .append("(")
                .append(e.getFileName())
                .append(":")
                .append(e.getLineNumber())
                .append(")")
                .append("\n");

            if (++count >= maxDepth) break;
        }

        return sb.toString().trim();
    }
}
