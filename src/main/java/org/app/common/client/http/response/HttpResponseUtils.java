package org.app.common.client.http.response;

import com.fasterxml.jackson.databind.JavaType;
import org.app.common.utils.JacksonUtils;

import java.net.http.HttpResponse;

import static jdk.internal.net.http.common.Utils.charsetFrom;

public class HttpResponseUtils {

    private HttpResponseUtils () {
        // Utility class, no instantiation
    }

    @SuppressWarnings("unchecked")
    public static <T> HttpResponse.BodyHandler<T> generic(JavaType type) {
        return responseInfo -> {
            HttpResponse.BodySubscriber<String> upstream = HttpResponse.BodySubscribers.ofString(charsetFrom(responseInfo.headers()));
            return HttpResponse.BodySubscribers.mapping(upstream, (String body) -> {
                if (type.hasRawClass(String.class)) {
                    return (T) body;
                }
                return JacksonUtils.readValue(body, type);
            });
        };
    }
}
