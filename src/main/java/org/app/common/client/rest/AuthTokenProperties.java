package org.app.common.client.rest;

import lombok.Getter;
import lombok.Setter;
import org.app.common.client.AuthTokenInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthTokenProperties implements AuthTokenInfo {

    private String URI;

    private String token;

    @Getter
    private HttpMethod method;

    private Map<String, String> body;

    @Getter
    private int timeout = 5000; // Default timeout in milliseconds

    @Override
    public String getURI() {
        return URI;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return method;
    }

    @Override
    public Map<String, String> getBody() {
        return body;
    }

    @Override
    public int timeout() {
        return timeout;
    }
}
