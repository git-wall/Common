package org.app.common.interceptor;

import brave.Span;
import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.app.common.context.DecorateContext;
import org.app.common.entities.log.EntityUserLog;
import org.app.common.entities.log.IEntity;
import org.app.common.entities.log.TracingLog;
import org.app.common.thread.AutoRun;
import org.app.common.thread.RunnableProvider;
import org.app.common.utils.MapperUtils;
import org.app.common.utils.RequestUtils;
import org.app.common.utils.TokenUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Monitor request log
 * */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MonitorLog {

    private final Queue<IEntity> logsQueue = new ConcurrentLinkedQueue<>();

    private final Tracer tracer;

    @Value("${spring.application.name}")
    private String application;

    @Around("@annotation(InterceptorLog)")
    public Object monitorApi(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = RequestUtils.getHttpServletRequest();

        before(request);

        IEntity logEntity = null;
        TracingLog tracingLog = getTracingLog(request, joinPoint);
        long startTime = System.currentTimeMillis();
        try {
            logEntity = startCollect(request, LogLevel.INFO.ordinal(), tracingLog);
            Object result = joinPoint.proceed();
            endCollect(System.currentTimeMillis() - startTime, result, tracingLog);
            return result;
        } catch (Exception exception) {
            logEntity = startCollect(request, LogLevel.ERROR.ordinal(), tracingLog);
            endCollect(0L, exception, tracingLog);
            throw exception;
        } finally {
            after(logEntity);
        }
    }

    private void before(HttpServletRequest httpServletRequest) {
        Optional.ofNullable(httpServletRequest)
                .map(RequestUtils::getRequestId)
                .ifPresentOrElse(
                        requestId -> DecorateContext.put(RequestUtils.REQUEST_ID, requestId),
                        () -> DecorateContext.put(RequestUtils.REQUEST_ID, getSpan().context().traceIdString())
                );
    }

    private void after(IEntity entity) {
        DecorateContext.clear();
        if (entity != null) logsQueue.offer(entity);
    }

    private void endCollect(long executeDuration, Object response, TracingLog tracingLog) {
        tracingLog.setExecuteDuration(executeDuration);
        tracingLog.setResponse(MapperUtils.toString(response));
    }

    private IEntity startCollect(HttpServletRequest hsr, int ordinal, TracingLog tracingLog) {
        EntityUserLog e = new EntityUserLog();
        e.setTracingLog(tracingLog);
        e.setLevel(ordinal);
        e.setId(DecorateContext.get(RequestUtils.REQUEST_ID));
        e.setApplication(application);
        e.setHeaders(RequestUtils.getRequestHeaders(hsr));
        e.setIp(RequestUtils.getRemoteAddress(hsr));
        e.setSource(RequestUtils.getDomain(hsr));
        e.setTime(new Date().getTime());

        return e;
    }

    @NonNull
    private TracingLog getTracingLog(HttpServletRequest hsr, ProceedingJoinPoint jp) {
        TracingLog tracingLog = new TracingLog();
        tracingLog.setTracID(getSpan().context().traceIdString());
        tracingLog.setRequest(getRequestAsString(jp));
        tracingLog.setUrl(RequestUtils.getUrl(hsr));
        tracingLog.setMethod(((MethodSignature) jp.getSignature()).getMethod().getName());
        return tracingLog;
    }

    private String getRequestAsString(ProceedingJoinPoint joinPoint) {
        return Optional.of(joinPoint.getArgs())
                .filter(ArrayUtils::isNotEmpty)
                .map(this::getRequest)
                .orElse("");
    }

    @SneakyThrows
    private String getRequest(Object[] args) {
        return Arrays.stream(args)
                .map(e -> String.format(
                        "(request) %s : [%s]",
                        e.getClass().getName(),
                        MapperUtils.toJson(e))
                )
                .collect(Collectors.joining("\n\r"));
    }

    public Span getSpan() {
        return Optional.ofNullable(tracer.currentSpan()).orElse(tracer.nextSpan());
    }

    @AutoRun
    public class ScheduledReport extends RunnableProvider {
        @Override
        public void before() {
            log.info(this.getClass().getSimpleName(), " ready to run");
        }

        @Override
        public void now() {
            IEntity entity = logsQueue.poll();
            if (entity == null) return;
            String token = TokenUtils.generateId("logger", 16);
            log.info(token, entity);
        }

        @Override
        public void after() {
            log.info(this.getClass().getSimpleName(), " close");
        }
    }
}
