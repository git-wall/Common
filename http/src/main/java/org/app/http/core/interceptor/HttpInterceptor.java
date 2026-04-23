<<<<<<<< HEAD:http/src/main/java/org/app/http/interceptor/HttpInterceptor.java
package org.app.http.interceptor;

import org.app.http.request.MarkerRequest;
========
package org.app.http.core.interceptor;

import org.app.http.core.request.MarkerRequest;
>>>>>>>> dev:http/src/main/java/org/app/http/core/interceptor/HttpInterceptor.java

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

@FunctionalInterface
public interface HttpInterceptor {
    <T> HttpResponse<T> intercept(MarkerRequest markerRequest, Chain chain, HttpClient client);

    interface Chain {
        <T> HttpResponse<T> proceed(MarkerRequest markerRequest);
    }
}


