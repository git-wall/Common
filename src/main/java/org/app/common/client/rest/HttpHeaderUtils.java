package org.app.common.client.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.util.Collections;

public class HttpHeaderUtils {
    public static HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

    public static HttpHeaders createHeaders(String authorization) {
        HttpHeaders headers = createHeaders();

        if (StringUtils.hasText(authorization))
            headers.set(HttpHeaders.AUTHORIZATION, authorization);

        return headers;
    }

    public static String token(String token) {
        return String.format("Bearer %s", token);
    }
}
