package org.app.common.scylla;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "scylla")
public class ScyllaProperties {
    private List<String> contactPoints;
    private String localDatacenter;
    private String keyspace;
    private int port = 9042;
    private String username;
    private String password;
    private int maxRequestsPerConnection = 1024;
    private int poolingMaxConnectionsPerHost = 8;
    private long socketConnectTimeout = 5000L;
    private long requestTimeout = 5000L;
    private boolean enabled = false;
}