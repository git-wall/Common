<<<<<<<< HEAD:http/src/main/java/org/app/http/interceptor/BasicTokenInterceptor.java
package org.app.http.interceptor;

import org.app.http.request.MarkerRequest;
========
package org.app.http.core.interceptor;

import org.app.http.core.request.MarkerRequest;
>>>>>>>> dev:http/src/main/java/org/app/http/core/interceptor/BasicTokenInterceptor.java

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
