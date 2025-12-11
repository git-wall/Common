package org.app.common.design.revisited.retry;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Aspect
@Component
public class RetryAspect {

    @SneakyThrows
    @Around("@annotation(retryable)")
    public Object doRetry(ProceedingJoinPoint pjp, Retryable retryable) {
        // if disable, skip retry
        if (!RetryContext.isEnabled()) {
            return pjp.proceed();
        }

        int maxAttempts = retryable.maxAttempts();
        Duration delay = Duration.ofMillis(retryable.delay());
        Class<? extends Exception>[] errorType = retryable.value();

        Retry<Object> retry = new Retry<>(maxAttempts, delay, errorType);
        return retry.execute(pjp::proceed);
    }
}
