package org.app.common.contain;

public class TagURL {
    public static final String PING = "/ping";
    public static final String HEALTH = "/health";
    public static final String HEALTH_LIVE = "/health/live";
    public static final String HEALTH_READY = "/health/ready";
    public static final String HEALTH_SYSTEM = "/health/system";

    public static final String CREATE = "/create";
    public static final String UPDATE = "/update";
    public static final String DELETE = "/delete/{id}";
    public static final String FIND_BY_ID = "/findById/{id}";
    public static final String FIND_ALL = "/findAll";
}
