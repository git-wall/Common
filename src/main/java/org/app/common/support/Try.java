package org.app.common.support;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.function.Supplier;


@EnableRetry
public class Try {

    /////////////////////////////////////////////////
    /// Resilience annotation based retry wrapper ///
    /////////////////////////////////////////////////

    @Retry(name = "retry")
    public static <T> T retry(Supplier<T> operation) {
        return operation.get();
    }

    // Programmatic retry using RetryTemplate
    public static <T> T retry(Supplier<T> operation, int maxAttempts, long backoffPeriod) {
        RetryTemplate template = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);

        FixedBackOffPolicy backoffPolicy = new FixedBackOffPolicy();
        backoffPolicy.setBackOffPeriod(backoffPeriod);

        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backoffPolicy);

        return template.execute(context -> operation.get());
    }

    ////////////////////////////////////////////////////////////
    /// Retryable annotation based retry wrapper and Recover ///
    ////////////////////////////////////////////////////////////

    /// Need @EnableRetry if you want to use @Retryable
    /// Spring annotation based retry wrapper
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public static <T> T retryable(Supplier<T> operation) {
        return operation.get();
    }

    @Retryable(value = {RetryException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public static <T> T retryableWithException(Supplier<T> operation) throws RetryException {
        return operation.get();
    }

    @Recover
    public <T> T recover(RetryException e, Supplier<T> defaultV) {
        return defaultV.get();
    }
}
