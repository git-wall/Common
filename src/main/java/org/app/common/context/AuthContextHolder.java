package org.app.common.context;

import org.app.observation.context.AuthContext;

public final class AuthContextHolder {
    private static final ThreadLocal<AuthContext> CTX = new ThreadLocal<>();

    public static void set(AuthContext ctx) {
        CTX.set(ctx);
    }

    public static AuthContext get() {
        return CTX.get();
    }

    public static void clear() {
        CTX.remove();
    }
}
