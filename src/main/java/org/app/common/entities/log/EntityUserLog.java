package org.app.common.entities.log;

import lombok.Data;
import org.app.common.interceptor.log.InterceptorLog;

@Data
public class EntityUserLog implements IEntity {
    private String id;
    private String deviceId;
    private String accessToken;
    private String username;
    private String[] userAddress;
    private Integer level;
    private Long time;
    private String application;
    private String headers;
    private String ip;
    private String source;
    private TracingLog tracingLog;
    private String description;

    @Override
    public InterceptorLog.LogType getTracingLogType() {
        return tracingLog.getType();
    }
}
