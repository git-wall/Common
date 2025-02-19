package org.app.common.context;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * managing thread-local context.
 * However, the impact on the server depends on various factors,
 * including the server's threading model, the rate of incoming requests,
 * and the amount of data stored in the thread-local context.
 *
 * @apiNote if call put to add data make sure call clear when finish
 */
public class DecorateContext {

    private DecorateContext() {
    }

    /**
     * Initialization of ThreadLocal: Used ThreadLocal.withInitial() to initialize the ThreadLocal with a lambda expression.
     * This eliminates the need for explicit null checks.
     */
    private static final ThreadLocal<Map<String, String>> threadLocalContext = ThreadLocal.withInitial(HashMap::new);

    /**
     * put method: The put method now directly operates on the thread-local
     * context without the need for explicit null checks or setting the context back to the ThreadLocal.
     */
    public static void put(String key, String value) {
        Map<String, String> context = threadLocalContext.get();
        context.put(key, value);
    }

    public static String get(String key) {
        return threadLocalContext.get().getOrDefault(key, "N/A");
    }

    /**
     * clear method: The clear method now directly clears the context without additional null checks or removal from the ThreadLocal.
     */
    public static void clear() {
        threadLocalContext.remove();
    }

    /**
     * Safe remove
     * */
    @PreDestroy
    public void cleanup() {
        threadLocalContext.remove();
    }
}
