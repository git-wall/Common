package org.app.common.design.revisited.retry;

public class RetryContext {
    private static final ThreadLocal<Boolean> ENABLED = ThreadLocal.withInitial(() -> true);

    public static void enable() {
        ENABLED.set(true);
    }

    public static void disable() {
        ENABLED.set(false);
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static void clear() {
        ENABLED.remove();
    }
}
