package org.app.security.shield.event;

import org.app.security.shield.constant.AuthEventType;
import org.app.security.shield.constant.AuthFailureReason;

import java.time.Instant;

public class AuthSecurityEvent {

    public AuthEventType eventType;
    public AuthFailureReason failureReason;

    public String subject;
    public String service;
    public String path;
    public String method;
    public String ip;
    public String userAgent;

    public String traceId;
    public Instant timestamp;
}
