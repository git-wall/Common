package org.app.http.response;

import com.fasterxml.jackson.databind.JavaType;
import lombok.NoArgsConstructor;
import org.app.jackson.JacksonUtils;

import java.net.http.HttpResponse;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ResponseUtils {
    public static <T> HttpResponse.BodyHandler<T> generic(JavaType type) {
        return responseInfo -> {
            var upstream = HttpResponse.BodySubscribers.ofInputStream();
            return HttpResponse.BodySubscribers.mapping(
                upstream,
                body -> JacksonUtils.readValue(body, type)
            );
        };
    }
}
