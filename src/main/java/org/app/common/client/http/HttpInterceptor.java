package org.app.common.client.http;

import org.app.common.client.http.request.MarkerRequest;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

@FunctionalInterface
public interface HttpInterceptor {
    <T> HttpResponse<T> intercept(MarkerRequest markerRequest, Chain chain, HttpClient client);

    interface Chain {
        <T> HttpResponse<T> proceed(MarkerRequest markerRequest);
    }
}


