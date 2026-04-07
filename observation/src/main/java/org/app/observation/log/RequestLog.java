package org.app.observation.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog implements Serializable {
    private static final long serialVersionUID = -5034595360272353978L;

    /* ================= CORE ================= */
    private String requestId;
    private String traceId;
    private String application;

    /* ================= TIME ================= */
    private long time;   // epoch millis
    private long durationMs;

    /* ================= HTTP ================= */
    private String method;
    private String path;
    private int status;
    private String host;
    private String userAgent;

    /* ================= NETWORK ================= */
    private String clientIp;
    private String serviceIp;
    private String deviceId;
    private String curl;

    /* ================= AUTH (SAFE) ================= */
    private String subject;
    private String username;
    private String clientId;
    private String tenantId;
    private Set<String> roles;

    /* ================= TOKEN (SAFE HASH ONLY) ================= */
    private String tokenId;     // jti
    private String tokenHash;   // sha256(token)

    /* ================= PAYLOAD ================= */
    private String requestBody;
    private String responseBody;

    /* ================= EXTRA ================= */
    private Map<String, Object> attributes;
}

