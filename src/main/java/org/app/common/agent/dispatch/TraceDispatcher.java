package org.app.common.agent.dispatch;

import java.util.Map;

public interface TraceDispatcher {
    void dispatch(String traceId, Map<String, Object> context);
}
