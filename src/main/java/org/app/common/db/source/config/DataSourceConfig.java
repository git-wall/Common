package org.app.common.db.source.config;

import lombok.Data;

/**
 * Configuration properties for a datasource
 */
@Data
public class DataSourceConfig {
    private String name;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private int maxPoolSize = 10;
    private int minIdle = 5;
    private long connectionTimeout = 20000L;
    private long idleTimeout = 300000L;

    // Additional properties can be added as needed
    private String validationQuery = "SELECT 1";
    private boolean testOnBorrow = true;
    private boolean testWhileIdle = true;
    private int validationInterval = 30000;
}
