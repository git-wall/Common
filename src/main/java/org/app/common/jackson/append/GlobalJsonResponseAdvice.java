package org.app.common.jackson.append;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.app.common.context.SpringContext;
import org.app.common.context.TracingContext;
import org.app.common.utils.ClassUtils;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GlobalJsonResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;
    private final List<String> appendedFields;

    public GlobalJsonResponseAdvice(MixinConfig mixinConfig) {
        this.objectMapper = SpringContext.getBean("MixinJackson", ObjectMapper.class);

        // lookup from the registered mixin
        this.appendedFields = ClassUtils.getClassFieldNames(mixinConfig.getClass());
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // apply to all responses
    }

    @Override
    @SneakyThrows
    public Object beforeBodyWrite(
        Object body,
        MethodParameter returnType, MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        ServerHttpRequest request, ServerHttpResponse response) {

        if (appendedFields.isEmpty()) return body;

        // Build attributes dynamically based on declared mixin fields
        Map<Object, Object> attrs = new HashMap<>(3);
        for (String key : appendedFields) {
            switch (key) {
                case VirtualProps.TRACE_ID:
                    attrs.put(key, MDC.get("traceId"));
                    break;
                case VirtualProps.TIMESTAMP:
                    attrs.put(key, System.currentTimeMillis());
                    break;
                case VirtualProps.REQUEST_ID:
                    attrs.put(key, TracingContext.getRequestId());
                    break;
            }
        }

        String json = objectMapper
            .writerFor(Object.class)
            .withAttributes(attrs)
            .writeValueAsString(body);

        return objectMapper.readTree(json);
    }
}
