package org.app.common.client.http.request;

import com.fasterxml.jackson.databind.JavaType;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;

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
                requestBuilder = HttpRequestUtils.getBuilder(url);
                break;
            case "POST":
                requestBuilder = HttpRequestUtils.postBuilder(url, body, headers);
                break;
            case "PUT":
                requestBuilder = HttpRequestUtils.putBuilder(url, body, headers);
                break;
            case "DELETE":
                requestBuilder = HttpRequestUtils.deleteBuilder(url);
                break;
            default:
                break;
        }
    }

    public void basicTokenRequest(String token) {
        buildRequest();
        requestBuilder.header(HttpHeaders.AUTHORIZATION, "Basic " + token);
    }

    public void authRequest(String token) {
        buildRequest();
        requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    public String requestInfo() {
        return String.format("Request{URI: %s, Method: %s, Body: %s} ", url, method, body);
    }
}
