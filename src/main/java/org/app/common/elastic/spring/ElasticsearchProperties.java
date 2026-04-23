package org.app.common.elastic.spring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "elasticsearch")
@Configuration
@Data
public class ElasticsearchProperties {
    private String host;
    private int port;
    private String username;
    private String password;

    public String getHostAndPort() {
        return String.format("%s:%d", host, port);
    }
}
