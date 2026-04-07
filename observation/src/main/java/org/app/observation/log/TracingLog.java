package org.app.observation.log;

import lombok.Data;
import org.app.observation.context.ContextKey;
import org.slf4j.MDC;

import java.io.Serializable;

@Data
public class TracingLog implements Serializable {
    private static final long serialVersionUID = 1L;

    // correlation
    private String requestId;
    private String traceId;

    // semantic
    private String className;
    private String methodName;

    // request
    private String url;
    private String clientIp;

    // execution
    private long duration;
    private String request;
    private String response;

    public static TracingLog of(
        String className,
        String methodName,
        String requestPayload,
        String url,
        String clientIp
    ) {
        TracingLog log = new TracingLog();

        // correlation
        log.requestId = MDC.get(ContextKey.REQUEST_ID);
        log.traceId = MDC.get(ContextKey.TRACE_ID);

        // method info

        log.className = className;
        log.methodName = methodName;

        // request info
        log.url = url;
        log.clientIp = clientIp;

        // payload
        log.request = requestPayload;

        return log;
    }
}
