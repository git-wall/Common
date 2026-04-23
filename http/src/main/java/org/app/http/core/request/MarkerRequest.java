package org.app.http.core.request;

import com.fasterxml.jackson.databind.JavaType;
import lombok.Builder;
import lombok.Data;

import java.net.http.HttpRequest;
import java.util.Map;

@Data
@Builder
public class MarkerRequest {
    private HttpRequest.Builder requestBuilder;
    private String url;
    private String method;
    private String body;
    private Map<String, String> headers;
    private final JavaType javaType;
    private final boolean async;

    public void buildRequest() {
        switch (method) {
            case "GET":
                requestBuilder = RequestUtils.getBuilder(url);
                break;
            case "POST":
                requestBuilder = RequestUtils.postBuilder(url, body, headers);
                break;
            case "PUT":
                requestBuilder = RequestUtils.putBuilder(url, body, headers);
                break;
            case "DELETE":
                requestBuilder = RequestUtils.deleteBuilder(url);
                break;
            default:
                break;
        }
    }

    public void basicTokenRequest(String token) {
        buildRequest();
        requestBuilder.header("Authorization", "Basic " + token);
    }

    public void authRequest(String token) {
        buildRequest();
        requestBuilder.header("Authorization", "Bearer " + token);
    }

    public String requestInfo() {
        return String.format("Request{URI: %s, Method: %s, Body: %s} ", url, method, body);
    }
}
