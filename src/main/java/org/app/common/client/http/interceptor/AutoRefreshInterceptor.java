package org.app.common.client.http.interceptor;

import org.app.common.client.http.HttpInterceptor;
import org.app.common.client.http.request.MarkerRequest;
import org.springframework.http.HttpStatus;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

public class AutoRefreshInterceptor implements HttpInterceptor {
    private volatile String accessToken;
    private final Supplier<String> tokenSupplier; // function to get new token

    public AutoRefreshInterceptor(Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
        this.accessToken = tokenSupplier.get(); // get initial token
    }

    @Override
    public <T> HttpResponse<T> intercept(MarkerRequest wrapper, Chain chain, HttpClient client) {
        wrapper.authRequest(accessToken);
        HttpResponse<T> response = chain.proceed(wrapper);

        if (HttpStatus.UNAUTHORIZED.value() == response.statusCode()) {
            accessToken = tokenSupplier.get(); // refresh token
            wrapper.authRequest(accessToken);
            return chain.proceed(wrapper);
        }

        return response;
    }
}
