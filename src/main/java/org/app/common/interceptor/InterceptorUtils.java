package org.app.common.interceptor;

import org.app.common.client.AuthTokenInfo;
import org.app.common.client.ClientBasicAuthInfo;
import org.app.common.interceptor.rest.AuthRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * Utility class for creating HTTP request interceptors
 */
public class InterceptorUtils {

    /**
     * Creates an interceptor that adds authentication headers to requests
     * and handles token refresh when needed
     *
     * @param authTokenInfo Information about the authentication token
     * @return ClientHttpRequestInterceptor that handles authentication
     */
    public static ClientHttpRequestInterceptor basicAuthInterceptor(AuthTokenInfo authTokenInfo, ClientBasicAuthInfo clientBasicAuthInfo) {
        return new AuthRequestInterceptor(
                clientBasicAuthInfo.getBaseUrl(),
                authTokenInfo.getToken(),
                authTokenInfo::refreshToken  // Use the refreshToken method for token refresh
        );
    }
}
