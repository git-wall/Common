package org.app.common.context;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * --------------------------------------- <br/>
 * Class nên chứa:  <br/>
 * - correlation nội bộ <br/>
 * - flags <br/>
 * - execution state <br/>
 * --------------------------------------- <br/>
 * Ví dụ: <br/>
 * - TracingContext.put("retryCount", 3); <br/>
 * - TracingContext.put("abTest", "A");
 * */
public class TracingContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    public static Map<String, Object> getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        var x = CONTEXT.get();
        if (!x.isEmpty()) {
            x.clear();
        }
        CONTEXT.remove();
    }

    @PreDestroy
    public void cleanup() {
        clear();
    }
}
