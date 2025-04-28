package org.app.common.context;

import org.app.common.utils.RequestUtils;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

public class TracingContext {

    private static final ThreadLocal<Map<String, String>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, String value) {
        CONTEXT.get().put(key, value);
    }

    public static String get(String key) {
        return CONTEXT.get().getOrDefault(key, "N/A");
    }

    public static void putRequestId(String requestId) {
        put(RequestUtils.REQUEST_ID, requestId);
    }

    public static String getRequestId() {
        return get(RequestUtils.REQUEST_ID);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    @PreDestroy
    public void cleanup() {
        clear();
    }
}
