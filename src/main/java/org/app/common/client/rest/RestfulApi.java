package org.app.common.client.rest;

import org.app.common.client.AuthTokenInfo;
import org.app.common.client.ClientBasicAuthInfo;
import org.app.common.utils.JacksonUtils;
import org.app.common.utils.RestTemplateUtils;
import org.slf4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Fluent API for making REST calls
 * Provides a builder pattern for configuring and executing REST requests
 *
 * @param <T> The type of response expected from the API call
 */
public class RestfulApi<T> {
    // for call API
    private HttpHeaders headers;
    private RestTemplate client;
    private String url;
    private HttpMethod method;

    // for INPUT/OUTPUT
    private Object request;
    private T response;

    // for exception
    private Logger logger;
    private String error;

    /**
     * Default constructor
     */
    public RestfulApi() {
    }

    /**
     * Constructor with headers and client
     *
     * @param headers HTTP headers to use for requests
     * @param client RestTemplate client to use for requests
     */
    public RestfulApi(HttpHeaders headers, RestTemplate client) {
        this.client = client;
        this.headers = headers;
    }

    /**
     * Creates a RestfulApi instance with the default configuration
     *
     * @param restTemplate RestTemplate to use, or null to create a new one
     * @return RestfulApi instance
     */
    public static <T> RestfulApi<T> of(RestTemplate restTemplate) {
        var client = restTemplate == null
                ? RestTemplateUtils.build(5000)
                : restTemplate;
        return new RestfulApi<>(
                HeaderUtils.createHeaders(),
                client
        );
    }

    /**
     * Creates a RestfulApi instance with authentication token
     *
     * @param restTemplate RestTemplate to use, or null to create a new one
     * @param authToken Authentication token to use
     * @return RestfulApi instance with authentication
     */
    public static <T> RestfulApi<T> ofAuth(RestTemplate restTemplate, String authToken) {
        var client = restTemplate == null
                ? RestTemplateUtils.build(5000)
                : restTemplate;

        return new RestfulApi<>(
                HeaderUtils.createHeaders(authToken),
                client
        );
    }

    /**
     * Creates a RestfulApi instance with token refresh capability
     *
     * @param authTokenInfo Information about the authentication token
     * @param clientInfo Information about the client
     * @return RestfulApi instance with token refresh capability
     */
    public static <T> RestfulApi<T> ofAuthWithRefresh(AuthTokenInfo authTokenInfo, ClientBasicAuthInfo clientInfo) {
        RestTemplate client = RestTemplateUtils.buildWithTokenRefresh(authTokenInfo, clientInfo);

        return new RestfulApi<>(
                HeaderUtils.createHeaders(),  // Headers will be added by the interceptor
                client
        );
    }

    public RestfulApi<T> header(HttpHeaders headers) {
        this.headers = headers == null ? HeaderUtils.createHeaders() : headers;
        return this;
    }

    public RestfulApi<T> client(RestTemplate restTemplate) {
        this.client = restTemplate == null
                ? RestTemplateUtils.build(5000)
                : restTemplate;
        return this;
    }

    public RestfulApi<T> client(int timeout, ClientHttpRequestInterceptor... interceptors) {
        this.client = RestTemplateUtils.build(timeout, interceptors);
        return this;
    }

    public RestfulApi<T> addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public RestfulApi<T> request(Object request) {
        this.request = request;
        return this;
    }

    public RestfulApi<T> url(String url) {
        this.url = url;
        return this;
    }

    public RestfulApi<T> method(HttpMethod method) {
        this.method = method;
        return this;
    }

    @Valid
    public RestfulApi<T> exception(
            @NotNull(message = "Logger for build RestTemplate not null") Logger logger,
            @NotNull(message = "title for build RestTemplate not null") String error) {
        this.logger = logger;
        this.error = error;
        return this;
    }

    public RestfulApi<T> exchange() {
        HttpEntity<?> entity = new HttpEntity<>(request, headers);
        ParameterizedTypeReference<T> type = new ParameterizedTypeReference<>() {
        };

        if (logger != null)
            executeCatch(entity, type);
        else
            execute(entity, type);

        return this;
    }

    private void executeCatch(HttpEntity<?> entity, ParameterizedTypeReference<T> type) {
        try {
            execute(entity, type);
        } catch (Exception e) {
            logger.error(error, e);
            throw e;
        }
    }

    private void execute(HttpEntity<?> entity, ParameterizedTypeReference<T> type) {
        ResponseEntity<T> response = client.exchange(url, method, entity, type);
        this.response = response.getBody();
    }

    public T get() {
        return response;
    }

    public <R> R sink(Class<R> clazz) {
        return JacksonUtils.readValue(response.toString(), clazz);
    }

    public <R> R sinkToList(Class<R> clazz) {
        return JacksonUtils.readValue(response.toString(), clazz);
    }

    public Optional<T> getOptional() {
        return Optional.ofNullable(response);
    }
}
