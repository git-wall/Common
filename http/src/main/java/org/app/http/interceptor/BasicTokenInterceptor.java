package org.app.http.interceptor;

import org.app.http.request.MarkerRequest;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class BasicTokenInterceptor implements HttpInterceptor {
    private final String token;

    public BasicTokenInterceptor(String token) {
        this.token = token;
    }

    @Override
    public <T> HttpResponse<T> intercept(MarkerRequest wrapper, Chain chain, HttpClient client) {
        wrapper.basicTokenRequest(token);
        return chain.proceed(wrapper);
    }
}
