package org.app.common.interceptor;

import brave.Tracer;
import lombok.RequiredArgsConstructor;
import org.app.common.context.TracingContext;
import org.app.common.utils.RequestUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class RequestIdInterceptor implements HandlerInterceptor {

    private final Tracer tracer;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String requestId = Optional.of(request)
                .map(RequestUtils::getRequestId)
                .orElse(Optional.ofNullable(tracer.currentSpan())
                        .orElse(tracer.nextSpan())
                        .context()
                        .traceIdString());

        TracingContext.put(RequestUtils.REQUEST_ID, requestId);

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        TracingContext.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
