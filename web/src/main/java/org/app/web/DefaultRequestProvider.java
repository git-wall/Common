package org.app.web;

import javax.servlet.http.HttpServletRequest;

public class DefaultRequestProvider implements RequestProvider {
    private static final ThreadLocal<HttpServletRequest> HOLDER = new ThreadLocal<>();

    @Override
    public HttpServletRequest getCurrentRequest() {
        return HOLDER.get();
    }

    public static void set(HttpServletRequest req) { HOLDER.set(req); }
    public static void clear() { HOLDER.remove(); }
}
