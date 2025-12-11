package org.app.common.interceptor.log;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.app.common.context.TracingContext;
import org.app.common.design.revisited.PoisonPill;
import org.app.common.entities.log.RequestLog;
import org.app.common.entities.log.TracingLog;
import org.app.common.kafka.multi.BrokerManager;
import org.app.common.support.Travel;
import org.app.common.trace.Trace;
import org.app.common.utils.JacksonUtils;
import org.app.common.utils.RequestUtils;
import org.app.common.utils.TokenUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
|        M O N I T O R   L O G        |
\*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
@Aspect
@Component
@Slf4j
public class MonitorLog {

    @Value("${spring.application.name}")
    private String application;

    @Value("${monitor.log.kafka.topic}")
    private String topic;

    @Value("${monitor.log.kafka.brokerId}")
    private String brokerId;

    private final KafkaProducer<String, String> kafkaProducer;

    private final Trace trace;

    private final PoisonPill<RequestLog> pill;

    private final boolean isKafkaReady;

    public MonitorLog(Trace trace, BrokerManager brokerManager) {
        this.trace = trace;
        this.kafkaProducer = brokerManager.getProducer(brokerId);
        this.isKafkaReady = topic != null && kafkaProducer != null;
        this.pill = PoisonPill.beanPrototype(); // new prototype instance
        this.pill.setting(MonitorLog.class.getSimpleName(), RequestLog.EMPTY, this::offer);
    }

    @Around("@annotation(interceptorLog)")
    @SneakyThrows
    public Object monitorApi(ProceedingJoinPoint jp, InterceptorLog interceptorLog) {
        if (interceptorLog == null) return jp.proceed();

        var hsr = RequestUtils.getHttpServletRequest();
        TracingContext.extractRequestId(hsr, trace::getId);

        var tracingLog = TracingLog.of(hsr, jp, interceptorLog, trace);
        var entity = RequestLog.of(hsr, LogLevel.INFO.ordinal(), tracingLog, application);

        try {
            var tuple2 = Travel.result$timer(() -> Travel.process(jp));

            tracingLog.enrich(tuple2._2, tuple2._1);
            return tuple2._1;
        } catch (Exception e) {
            entity.setLevel(LogLevel.ERROR.ordinal());
            tracingLog.enrich(0L, e);
            throw e;
        } finally {
            pill.offer(entity);
        }
    }

    private void offer(RequestLog entity) {
        var token = TokenUtils.generateId("log_user", 16);
        var key = String.format("%s:%s", application, token);

        if (isKafkaLog(entity)) {
            kafkaProducer.send(new ProducerRecord<>(topic, key, JacksonUtils.toJson(entity)));
        } else {
            log.info("Key {} Info {}", key, entity);
        }
    }

    private boolean isKafkaLog(RequestLog entity) {
        return InterceptorLog.Check.isKafka(entity.getTracingLogType()) && isKafkaReady;
    }
}
