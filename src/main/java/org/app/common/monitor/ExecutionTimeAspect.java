package org.app.common.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.context.ConextKey;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionTimeAspect {

    @Around("@annotation(logExecutionTime)")
    public Object logTime(
            ProceedingJoinPoint joinPoint,
            ExecutionTime logExecutionTime) throws Throwable {

        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long end = System.currentTimeMillis();
            long duration = end - start;
            String customName = logExecutionTime.value();
            String requestId = MDC.get(ConextKey.REQUEST_ID);
            log.info("ID {} | Name {} executed in {} ms", requestId, customName, duration);
        }
    }
}
