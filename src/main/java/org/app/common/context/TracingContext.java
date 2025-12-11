package org.app.common.context;

import org.app.common.utils.RequestUtils;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TracingContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    public static void extractRequestId(HttpServletRequest httpServletRequest, Supplier<String> supplier) {
        if (getRequestId() != null) return;

        var requestId = RequestUtils.getRequestIdOrElse(httpServletRequest, supplier);

        putRequestId(requestId);
    }

    public static void putRequestId(String requestId) {
        put(RequestUtils.REQUEST_ID, requestId);
    }

    public static String getRequestId() {
        return get(RequestUtils.REQUEST_ID).toString();
    }

    public static Map<String, Object> getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        var x = CONTEXT.get();
        if (x != null && !x.isEmpty()) {
            x.clear();
        }
        CONTEXT.remove();
    }

    @PreDestroy
    public void cleanup() {
        clear();
    }
}
