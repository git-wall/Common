package org.app.observation.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContextHolder {

    private static final ThreadLocal<RequestContext> REQ = new ThreadLocal<>();
    private static final ThreadLocal<AuthContext> AUTH = new ThreadLocal<>();

    public static void setRequest(RequestContext ctx) {
        REQ.set(ctx);
    }

    public static RequestContext request() {
        return REQ.get();
    }

    public static void setAuth(AuthContext ctx) {
        AUTH.set(ctx);
    }

    public static AuthContext auth() {
        return AUTH.get();
    }

    public static void clearAuth() {
        AUTH.remove();
    }

    public static void clearRequest() {
        REQ.remove();
    }
}
