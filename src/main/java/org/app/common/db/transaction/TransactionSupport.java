package org.app.common.db.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class TransactionSupport {

    private final PlatformTransactionManager transactionManager;
    private final Map<String, RetryTemplate> retryTemplateMap = new ConcurrentHashMap<>();
    private  final Map<String, TransactionTemplate> txTemplateMap = new ConcurrentHashMap<>();

    // --- Đăng ký retry ---
    public RetryTemplate registerRetry(String key,
                                       int maxAttempts,
                                       long delay,
                                       double multiplier,
                                       Map<Class<? extends Throwable>, Boolean> retryableExceptions) {

        return retryTemplateMap.computeIfAbsent(
                key, k -> {
                    RetryTemplate template = new RetryTemplate();

                    SimpleRetryPolicy policy = new SimpleRetryPolicy(maxAttempts, retryableExceptions);
                    template.setRetryPolicy(policy);

                    ExponentialBackOffPolicy backoff = new ExponentialBackOffPolicy();
                    backoff.setInitialInterval(delay);
                    backoff.setMultiplier(multiplier);
                    template.setBackOffPolicy(backoff);

                    return template;
                }
        );
    }

    // --- Đăng ký transaction ---
    public TransactionTemplate registerTransaction(String key,
                                                   int timeoutSeconds,
                                                   Isolation isolation,
                                                   Propagation propagation) {

        return txTemplateMap.computeIfAbsent(
                key, k -> {
                    TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
                    txTemplate.setTimeout(timeoutSeconds);
                    txTemplate.setIsolationLevelName(isolation.name());
                    txTemplate.setPropagationBehaviorName(propagation.name());
                    return txTemplate;
                }
        );
    }

    // --- Thực thi với retry + transaction ---
    public  <T> T execute(String retryKey, String txKey, Supplier<T> action) {
        RetryTemplate retryTemplate = retryTemplateMap.get(retryKey);
        TransactionTemplate txTemplate = txTemplateMap.get(txKey);

        if (retryTemplate == null)
            throw new IllegalArgumentException("Retry config not found: " + retryKey);

        if (txTemplate == null)
            throw new IllegalArgumentException("Transaction config not found: " + txKey);

        return retryTemplate.execute(ctx ->
                txTemplate.execute(status -> action.get())
        );
    }

    // --- Overload: chỉ transaction, không retry ---
    public  <T> T executeTx(String txKey, Supplier<T> action) {
        TransactionTemplate txTemplate = txTemplateMap.get(txKey);
        if (txTemplate == null)
            throw new IllegalArgumentException("Transaction config not found: " + txKey);

        return txTemplate.execute(status -> action.get());
    }

    // --- Overload: chỉ retry, không transaction ---
    public  <T> T executeRetry(String retryKey, Supplier<T> action) {
        RetryTemplate retryTemplate = retryTemplateMap.get(retryKey);
        if (retryTemplate == null)
            throw new IllegalArgumentException("Retry config not found: " + retryKey);

        return retryTemplate.execute(ctx -> action.get());
    }
}
