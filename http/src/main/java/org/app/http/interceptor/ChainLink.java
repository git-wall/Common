package org.app.http.interceptor;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.app.http.ClientUtils;
import org.app.http.request.MarkerRequest;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Iterator;

@AllArgsConstructor
public class ChainLink implements HttpInterceptor.Chain {
    private final Iterator<HttpInterceptor> iterators;
    private final HttpClient client;

    @Override
    @SneakyThrows
    public <T> HttpResponse<T> proceed(MarkerRequest wrapper) {
        if (iterators.hasNext()) {
            return callChainLink(wrapper);
        }

        return callApi(wrapper);
    }

    private <T> HttpResponse<T> callChainLink(MarkerRequest wrapper) {
        HttpInterceptor interceptor = iterators.next();
        return interceptor.intercept(wrapper, this, client);
    }

    private <T> HttpResponse<T> callApi(MarkerRequest wrapper) {
        var request = wrapper.getRequestBuilder().build();
        var typeResponse = wrapper.getJavaType();

        return wrapper.isAsync()
            ? ClientUtils.Async.call(client, request, typeResponse)
            : ClientUtils.call(client, request, typeResponse);
    }
}
