package org.app.common.client.rest;

import lombok.Setter;
import org.app.common.client.ClientBasicAuthInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.client")
@Setter
public class ClientProperties implements ClientBasicAuthInfo {

    private String serviceName;
    private String hostName;
    private String baseUrl;
    private int port = 80; // Default port for HTTP
    private String schema = "http"; // Default schema is http
    private int timeout = 5000; // Default timeout in milliseconds

    private String username;
    private String password;
    private boolean bufferRequestBody = true;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isBufferRequestBody() {
        return bufferRequestBody;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getHostname() {
        return hostName;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public int timeout() {
        return timeout;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }
}
