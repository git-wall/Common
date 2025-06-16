package org.app.common.entities.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.common.interceptor.log.LogMonitor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    public LogMonitor.LogType[] getTracingLogType() {
        return tracingLog.getType();
    }
}
