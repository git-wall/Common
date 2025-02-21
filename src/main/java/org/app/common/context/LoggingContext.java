package org.app.common.context;

import org.slf4j.MDC;

public class LoggingContext {
    public static void setRequestId(String requestId) {
        MDC.put("requestId", requestId);
    }

    public static void clear() {
        MDC.clear();
    }
}
