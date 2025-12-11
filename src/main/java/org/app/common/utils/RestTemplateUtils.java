package org.app.common.utils;

import org.app.common.client.AuthTokenInfo;
import org.app.common.client.ClientBasicAuthInfo;
import org.app.common.client.rest.ClientBasicAuthFactory;
import org.app.common.client.rest.interceptor.AuthRequestInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class RestTemplateUtils {

    /**
     * Builds a RestTemplate with token authentication and automatic token refresh capability
     *
     * @param baseUrl       Base URL for the API
     * @param initialToken  Initial authentication token
     * @param tokenSupplier Supplier function to get a fresh token when needed
     * @param clientInfo    Information about the client
     * @return RestTemplate configured with token authentication and refresh capability
     */
    public static RestTemplate buildWithTokenRefresh(String baseUrl, String initialToken,
                                                     Supplier<String> tokenSupplier, ClientBasicAuthInfo clientInfo) {
        ClientBasicAuthFactory basicAuthFactory = ClientBasicAuthFactory.of(clientInfo);
        AuthRequestInterceptor authInterceptor = new AuthRequestInterceptor(baseUrl, initialToken, tokenSupplier);

        RestTemplate restTemplate = new RestTemplate(basicAuthFactory);
        restTemplate.setInterceptors(List.of(authInterceptor));
        return restTemplate;
    }

    /**
     * Builds a RestTemplate with token authentication and automatic token refresh capability
     * using AuthTokenInfo and ClientInfo
     *
     * @param authTokenInfo Information about the authentication token
     * @param clientInfo    Information about the client
     * @return RestTemplate configured with token authentication and refresh capability
     */
    public static RestTemplate buildWithTokenRefresh(AuthTokenInfo authTokenInfo, ClientBasicAuthInfo clientInfo) {
        ClientBasicAuthFactory basicAuthFactory = ClientBasicAuthFactory.of(clientInfo);

        AuthRequestInterceptor authInterceptor = new AuthRequestInterceptor(
                clientInfo.getBaseUrl(), null, authTokenInfo::refreshToken
        );

        RestTemplate restTemplate = new RestTemplate(basicAuthFactory);
        restTemplate.setInterceptors(List.of(authInterceptor));
        return restTemplate;
    }

    public static RestTemplate buildBasicAuth(ClientBasicAuthInfo clientInfo, ClientHttpRequestInterceptor... interceptors) {
        ClientBasicAuthFactory basicAuthFactory = ClientBasicAuthFactory.of(clientInfo);
        RestTemplate restTemplate = new RestTemplate(basicAuthFactory);
        restTemplate.setInterceptors(Arrays.asList(interceptors));
        return restTemplate;
    }

    public static RestTemplate build(int timeout, ClientHttpRequestInterceptor... interceptors) {
        RestTemplateBuilder builder = new RestTemplateBuilder();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        BufferingClientHttpRequestFactory bufferingRequestFactory = new BufferingClientHttpRequestFactory(requestFactory);

        return builder
                .requestFactory(() -> bufferingRequestFactory)
                .interceptors(interceptors)
                .build();
    }

    public static boolean shouldBufferRequestBody(Object body) {
        if (body == null) return true;

        // If it's a File or something large: use streaming (false)
        if (body instanceof File) {
            File file = (File) body;
            long sizeInMB = file.length() / (1024 * 1024);
            return sizeInMB < 10; // Stream if >=10MB
        }

        // Add more cases: InputStream, ByteArray, etc.
        if (body instanceof java.io.InputStream) {
            return false; // stream input
        }

        if (body instanceof byte[]) {
            byte[] bytes = (byte[]) body;
            return bytes.length < 10 * 1024 * 1024; // buffer if <10MB
        }

        // JSON objects, small text -> safe to buffer
        return true;
    }
}
