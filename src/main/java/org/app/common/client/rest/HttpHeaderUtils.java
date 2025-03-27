package org.app.common.client.rest;

import org.app.common.utils.RequestUtils;
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
            headers.set(RequestUtils.TOKEN_HEADER, authorization);

        return headers;
    }
}
