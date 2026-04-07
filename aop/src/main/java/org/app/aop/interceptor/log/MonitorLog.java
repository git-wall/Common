package org.app.aop.interceptor.log;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.app.jackson.JacksonUtils;
import org.app.message.send.Publisher;
import org.app.observation.context.AuthContext;
import org.app.observation.context.ContextHolder;
import org.app.observation.context.RequestContext;
import org.app.observation.log.RequestLog;
import org.app.observation.log.TracingLog;
import org.app.web.RequestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
|        M O N I T O R   L O G        |
\*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
@Aspect
@Slf4j
public class MonitorLog   {
    private final Publisher publisher;
    private final String application;

    public MonitorLog(String application, Publisher publisher) {
        this.application = application;
        this.publisher = publisher;
    }

    @Around("@annotation(interceptorLog)")
    @SneakyThrows
    public Object monitorApi(ProceedingJoinPoint jp, InterceptorLog interceptorLog) {
        HttpServletRequest request = RequestUtils.getRequest();
        MethodSignature sig = (MethodSignature) jp.getSignature();
        String param = Optional.of(jp.getArgs())
            .filter( e -> e.length > 0)
            .map(JacksonUtils::writeValueAsString)
            .orElse("");
        TracingLog tracingLog = TracingLog.of(
            sig.getDeclaringTypeName(),
            sig.getMethod().getName(),
            param,
            RequestUtils.getUrlNoParams(request),
            RequestUtils.getRemoteAddress(request)
        );

        Object result = null;
        Throwable error = null;
        long start = System.currentTimeMillis();
        try {
            result = jp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;
            boolean isError = error != null;

            enrich(tracingLog, duration, isError ? error : result, interceptorLog.depth());
            if (isError) {
                log.error("TRACE {}", JacksonUtils.toJson(tracingLog));
            } else
                log.info("TRACE {}", JacksonUtils.toJson(tracingLog));

            if (publisher != null && interceptorLog.root()) {
                RequestLog requestLog = build(tracingLog);
                publisher.publish(requestLog);
            }
        }
    }

    private RequestLog build(TracingLog tracingLog) {
        RequestContext req = ContextHolder.request();
        AuthContext auth = ContextHolder.auth();

        return RequestLog.builder()
            .requestId(req.getRequestId())
            .traceId(req.getRequestId())
            .application(application)

            .time(req.getTime())
            .durationMs(tracingLog.getDuration())

            .method(req.getMethod())
            .path(req.getPath())

            .clientIp(req.getClientIp())
            .serviceIp(req.getServiceIp())
            .deviceId(req.getDeviceId())
            .curl(req.getCurl())

            .subject(auth.getSubject())
            .username(auth.getUsername())
            .tenantId(auth.getTenantId())
            .roles(auth.getRoles())

            .tokenId(auth.getTokenId())
            .tokenHash(auth.getTokenHash())

            .requestBody(tracingLog.getRequest())
            .responseBody(tracingLog.getResponse())

            .build();
    }

    public void enrich(TracingLog tracingLog, long duration, Object result, int depth) {
        tracingLog.setDuration(duration);

        if (result instanceof Throwable) {
            tracingLog.setResponse(printStack((Throwable) result, depth));
        } else {
            tracingLog.setResponse(JacksonUtils.toJson(result));
        }
    }

    public static String printStack(Throwable t, int maxDepth) {
        if (maxDepth == 0)
            return t.getMessage();

        StringBuilder sb = new StringBuilder();

        sb.append(t.getClass().getSimpleName());
        sb.append(": ");
        sb.append(t.getMessage());
        sb.append("\n");

        int count = 0;

        for (StackTraceElement e : t.getStackTrace()) {
            sb.append(e.getClassName())
                .append(".")
                .append(e.getMethodName())
                .append("(")
                .append(e.getFileName())
                .append(":")
                .append(e.getLineNumber())
                .append(")")
                .append("\n");

            if (++count >= maxDepth) break;
        }

        return sb.toString().trim();
    }
}
