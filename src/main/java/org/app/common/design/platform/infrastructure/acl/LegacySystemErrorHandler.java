package org.app.common.design.platform.infrastructure.acl;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

@Component
public class LegacySystemErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        // Custom error handling for legacy system
        if (response.getStatusCode().is4xxClientError()) {
            throw new RuntimeException("Client error from legacy system");
        } else if (response.getStatusCode().is5xxServerError()) {
            throw new RuntimeException("Server error from legacy system");
        }
    }
}
