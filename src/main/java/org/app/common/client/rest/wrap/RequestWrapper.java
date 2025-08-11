package org.app.common.client.rest.wrap;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;

import java.net.URI;
import java.net.URISyntaxException;

public class RequestWrapper extends HttpRequestWrapper {

    private final String baseUrl;

    public RequestWrapper(HttpRequest request, String baseUrl) {
        super(request);
        this.baseUrl = baseUrl;
    }

    @Override
    public URI getURI() {
        try {
            return new URI(baseUrl + super.getURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
