package org.app.common.interceptor.log;

import brave.Span;
import brave.Tracer;
import io.vavr.Tuple2;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.app.common.context.TracingContext;
import org.app.common.design.revisited.PoisonPill;
import org.app.common.entities.log.EntityUserLog;
import org.app.common.entities.log.IEntity;
import org.app.common.entities.log.TracingLog;
import org.app.common.interceptor.Interceptor;
import org.app.common.kafka.multi.BrokerManager;
import org.app.common.support.Travel;
import org.app.common.utils.AuthUtils;
import org.app.common.utils.JacksonUtils;
import org.app.common.utils.RequestUtils;
import org.app.common.utils.TokenUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
        M O N I T O R   L O G
\*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
@Aspect
@Component
@Slf4j
public class MonitorLog extends Interceptor<LogMonitor> {

    private final Tracer tracer;

    @Value("${spring.application.name}")
    private String application;

    @Value("${monitor.log.kafka.topic}")
    private String topic;

    @Value("${monitor.log.kafka.brokerId}")
    private String brokerId;

    private final KafkaProducer<String, String> kafkaProducer;

    private PoisonPill<IEntity> pill;

    public MonitorLog(Tracer tracer, BrokerManager brokerManager) {
        this.tracer = tracer;
        this.kafkaProducer = brokerManager.getProducer(brokerId);
    }

    @PostConstruct
    public void init() {
        this.pill = PoisonPill.beanPrototype(); // new prototype instance
        this.pill.setting(
                "ScheduledLog",
                new EntityUserLog(),
                new ConcurrentLinkedQueue<>(),
                entity -> {
                    var token = TokenUtils.generateId("log_user", 16);
                    var key = String.format("%s:%s", application, token);

                    if (isKafkaLog(entity)) {
                        send(entity, key);
                    }

                    log.info(token, entity);
                }
        );
    }

    private static @NotNull Supplier<LogMonitor> getDefaultInterceptorLog() {
        return () -> new LogMonitor() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return LogMonitor.class;
            }

            @Override
            public LogType[] type() {
                return new LogType[]{LogType.GRAYLOG};
            }
        };
    }

    @Around(
//            "@within(org.springframework.web.bind.annotation.RestController) || " +
            "execution(@(@org.springframework.web.bind.annotation.RequestMapping *) * *(..)) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) ||" +
            "@annotation(org.app.common.interceptor.log.LogMonitor)"
    )
    public Object monitorApi(ProceedingJoinPoint joinPoint) {
        LogMonitor logMonitor = getOrDefault(joinPoint, LogMonitor.class, getDefaultInterceptorLog());

        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        before(request);

        IEntity logEntity = null;
        var tracingLog = getTracingLog(request, joinPoint, logMonitor);
        try {
            logEntity = startCollect(request, LogLevel.INFO.ordinal(), tracingLog);

            Tuple2<Object, Long> tuple2 = Travel.tuple$timer(() -> Travel.process(joinPoint));

            endCollect(tuple2._2, tuple2._1, tracingLog);
            return tuple2._1;
        } catch (Exception exception) {
            logEntity = startCollect(request, LogLevel.ERROR.ordinal(), tracingLog);
            endCollect(0L, exception, tracingLog);
            throw exception;
        } finally {
            after(logEntity);
        }
    }

    private void before(HttpServletRequest httpServletRequest) {
        if (TracingContext.getRequestId() != null) return;

        var requestId = Optional.ofNullable(httpServletRequest)
                .map(RequestUtils::getRequestId)
                .orElse(getSpan().context().traceIdString());

        TracingContext.putRequestId(requestId);
    }

    private void after(IEntity entity) {
        if (entity != null) {
            pill.offer(entity);
        }
    }

    private void endCollect(long executeDuration, Object response, TracingLog tracingLog) {
        tracingLog.setExecuteDuration(executeDuration);
        tracingLog.setResponse(JacksonUtils.toString(response));
    }

    private IEntity startCollect(HttpServletRequest hsr, int ordinal, TracingLog tracingLog) {
        EntityUserLog e = new EntityUserLog();
        e.setId(TokenUtils.generateId("log_user", 12));
        e.setDeviceId(RequestUtils.getDeviceId(hsr));
        e.setAccessToken(RequestUtils.getToken(hsr));
        e.setUsername(AuthUtils.getCurrentUsername());
        e.setUserAddress(new String[]{tracingLog.getUserAddress()});
        e.setLevel(ordinal);
        e.setTime(new Date().getTime());
        e.setHeaders(RequestUtils.getRequestHeaders(hsr));
        e.setIp(RequestUtils.getRemoteAddress(hsr));
        e.setSource(RequestUtils.getDomain(hsr));
        e.setTracingLog(tracingLog);
        e.setApplication(application);
        return e;
    }

    @NonNull
    private TracingLog getTracingLog(HttpServletRequest hsr, ProceedingJoinPoint jp, LogMonitor il) {
        TracingLog tracingLog = new TracingLog();
        tracingLog.setRequestId(TracingContext.getRequestId());
        tracingLog.setTracID(getSpan().context().traceIdString());
        tracingLog.setRequest(getRequestAsString(jp));
        tracingLog.setUrl(RequestUtils.getUrlNoParams(hsr));
        tracingLog.setMethod(((MethodSignature) jp.getSignature()).getMethod().getName());
        tracingLog.setType(il.type());
        tracingLog.setUserAddress(RequestUtils.getRemoteAddress(hsr));
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
                        JacksonUtils.toJson(e))
                )
                .collect(Collectors.joining("\n\r"));
    }

    public Span getSpan() {
        return Optional.ofNullable(tracer.currentSpan()).orElse(tracer.nextSpan());
    }

    private void send(IEntity entity, String key) {
        var val = JacksonUtils.toString(entity);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, val);
        kafkaProducer.send(record);
    }

    private boolean isKafkaLog(IEntity entity) {
        return hasKafka(entity)
                && topic != null
                && kafkaProducer != null;
    }

    private static boolean hasKafka(IEntity entity) {
        for (LogMonitor.LogType lt : entity.getTracingLogType()) {
            return lt == LogMonitor.LogType.KAFKA;
        }
        return false;
    }
}
