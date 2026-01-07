package org.app.common.client.http.response;

import com.fasterxml.jackson.databind.JavaType;
import lombok.NoArgsConstructor;
import org.app.common.utils.JacksonUtils;

import java.io.InputStream;
import java.net.http.HttpResponse;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class HttpResponseUtils {
    public static <T> HttpResponse.BodyHandler<T> generic(JavaType type) {
        return responseInfo -> {
            HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();
            return HttpResponse.BodySubscribers.mapping(upstream, body -> {
                return JacksonUtils.readValue(body, type);
            });
        };
    }
}
