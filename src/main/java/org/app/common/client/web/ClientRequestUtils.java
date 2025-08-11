package org.app.common.client.web;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;

public class ClientRequestUtils {
    public static ClientRequest createWithAuth(ClientRequest clientRequest, String accessToken) {
       return ClientRequest.from(clientRequest)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
    }
}
