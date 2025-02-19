package org.app.common.entities.log;

import lombok.Data;

@Data
public class EntityUserLog implements IEntity {
    private String id;
    private String deviceId;
    private String accessToken;
    private String username;
    private String token;
    private String[] userAddress;
    private Integer level;
    private Long time;
    private String description;
    private Integer day;
    private Integer month;
    private Integer year;
    private Integer responseStatus;
    private String application;
    private String headers;
    private String ip;
    private String source;
    private TracingLog tracingLog;
}
