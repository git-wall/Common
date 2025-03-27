package org.app.common.cassandra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.cassandra")
public class CassandraProperties {
    private boolean enabled = false;
    private String contactPoints;
    private int port = 9042;
    private String localDatacenter;
    private String keyspace;
    private String username;
    private String password;
    private int requestTimeout = 10000;
    private int connectTimeout = 10000;
    private Pool pool = new Pool();

    @Data
    public static class Pool {
        private int maxConnections = 8;
        private int maxRequests = 32768;
        private long heartbeatIntervalSeconds = 30L;
    }
}