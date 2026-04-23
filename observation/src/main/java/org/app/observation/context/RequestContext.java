package org.app.observation.context;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RequestContext {
    // correlation
    private String requestId;

    // request info
    private String method;
    private String path;
    private String host;        // Header Host
    private String domain;      // parsed từ Host
    private String userAgent;

    // network
    private String clientIp;    // IP người dùng thật
    private String serviceIp;   // IP instance xử lý request

    // device / trace
    private String deviceId;
    private String curl;

    private long time;

    private Object payload;

    private Map<String, Object> metadata;
}
