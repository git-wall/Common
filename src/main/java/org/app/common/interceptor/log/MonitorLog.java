package org.app.common.interceptor.log;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.app.common.context.TracingContext;
import org.app.common.entities.log.RequestLog;
import org.app.common.entities.log.TracingLog;
import org.app.common.job.ExecutorFactory;
import org.app.common.job.GrayLogJob;
import org.app.common.job.JobRunner;
import org.app.common.job.KafkaLogJob;
import org.app.common.kafka.multi.BrokerManager;
import org.app.common.support.Travel;
import org.app.common.trace.Trace;
import org.app.common.utils.RequestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
|        M O N I T O R   L O G        |
\*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
@Aspect
@Component
@Slf4j
public class MonitorLog implements DisposableBean {
    private final String application;
    private final Trace trace;
    private final JobRunner jobRunner;
    private final BlockingQueue<RequestLog> queue = new LinkedBlockingQueue<>();

    public MonitorLog(
        Trace trace,
        BrokerManager brokerManager,
        @Value("${spring.application.name}") String application,
        @Value("${monitor.log.kafka.topic}") String topic,
        @Value("${monitor.log.kafka.brokerId}") String brokerId) {
        this.trace = trace;
        this.application = application;

        ExecutorService executor = ExecutorFactory.create();
        this.jobRunner = new JobRunner(executor);

        if (topic != null) {
            jobRunner.submit(
                new KafkaLogJob(queue, brokerManager.getProducer(brokerId), topic, application)
            );
        } else {
            jobRunner.submit(
                new GrayLogJob(queue, application)
            );
        }
    }

    @Around("@annotation(interceptorLog)")
    @SneakyThrows
    public Object monitorApi(ProceedingJoinPoint jp, InterceptorLog interceptorLog) {
        if (interceptorLog == null) return jp.proceed();

        var hsr = RequestUtils.getCurrentHttpRequest();
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
            queue.add(entity);
        }
    }

    @Override
    @PreDestroy
    public void destroy() {
        log.info("Shutting down MonitorLog...");
        queue.add(RequestLog.EMPTY);
        jobRunner.close();
    }
}
