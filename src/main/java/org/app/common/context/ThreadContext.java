package org.app.common.context;

import lombok.NoArgsConstructor;
import org.app.common.utils.RequestUtils;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * Ids stored in the thread-local context.
 *
 * @apiNote if call put to add data make sure call clear when finish
 */
@NoArgsConstructor
public class ThreadContext {

    /**
     * Initialization of ThreadLocal: Used ThreadLocal.withInitial() to initialize the ThreadLocal with a lambda expression.
     * This eliminates the need for explicit null checks.
     */
    private static final ThreadLocal<Map<String, String>> SHADOW_THREAD = ThreadLocal.withInitial(HashMap::new);

    /**
     * put method: The put method now directly operates on the thread-local
     * context without the need for explicit null checks or setting the context back to the ThreadLocal.
     */
    public static void put(String key, String value) {
        Map<String, String> context = SHADOW_THREAD.get();
        context.put(key, value);
    }

    public static void putRequestId(String value) {
        Map<String, String> context = SHADOW_THREAD.get();
        context.put(RequestUtils.REQUEST_ID, value);
    }

    public static String get(String key) {
        return SHADOW_THREAD.get().getOrDefault(key, "N/A");
    }

    public static String getRequestId() {
        return SHADOW_THREAD.get().get(RequestUtils.REQUEST_ID);
    }

    /**
     * clear method: The clear method now directly clears the context without additional null checks or removal from the ThreadLocal.
     */
    public static void clear() {
        SHADOW_THREAD.remove();
    }

    /**
     * Safe remove
     * */
    @PreDestroy
    public void cleanup() {
        SHADOW_THREAD.remove();
    }
}
