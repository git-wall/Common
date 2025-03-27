package org.app.common.async;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for working with CompletableFuture and asynchronous operations.
 * Provides methods for chaining actions, handling multiple parallel actions,
 * and working with single asynchronous operations.
 */
@Slf4j
@Component
public class AsyncProcessor {

    private final ExecutorService executorService;

    public AsyncProcessor() {
        this.executorService = Executors.newWorkStealingPool(10);
    }

    public AsyncProcessor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Execute a task asynchronously
     *
     * @param task The task to execute
     * @param <T>  The return type
     * @return A CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executorService);
    }

    /**
     * Execute a task asynchronously with no return value
     *
     * @param task The task to execute
     * @return A CompletableFuture with no result
     */
    public CompletableFuture<Void> executeAsync(Runnable task) {
        return CompletableFuture.runAsync(task, executorService);
    }

    /**
     * Chain multiple transformations on a CompletableFuture
     *
     * @param initial         The initial value
     * @param transformations The transformations to apply
     * @param <T>             The initial type
     * @param <R>             The final type
     * @return A CompletableFuture with the final result
     */
    @SafeVarargs
    public final <T, R> CompletableFuture<R> chain(T initial, Function<Object, ?>... transformations) {
        CompletableFuture<Object> future = CompletableFuture.completedFuture(initial);

        for (Function<Object, ?> transformation : transformations) {
            future = future.thenApplyAsync(transformation, executorService);
        }

        return (CompletableFuture<R>) future;
    }

    /**
     * Chain multiple transformations on a CompletableFuture
     *
     * @param initialFuture   The initial CompletableFuture
     * @param transformations The transformations to apply
     * @param <T>             The initial type
     * @param <R>             The final type
     * @return A CompletableFuture with the final result
     */
    @SafeVarargs
    public final <T, R> CompletableFuture<R> chain(CompletableFuture<T> initialFuture,
                                                   Function<Object, ?>... transformations) {
        CompletableFuture<Object> future = (CompletableFuture<Object>) initialFuture;

        for (Function<Object, ?> transformation : transformations) {
            future = future.thenApplyAsync(transformation::apply, executorService);
        }

        return (CompletableFuture<R>) future;
    }

    /**
     * Execute multiple tasks in parallel and wait for all to complete
     *
     * @param tasks The tasks to execute
     * @param <T>   The return type
     * @return A CompletableFuture with a list of results
     */
    public <T> CompletableFuture<List<T>> executeAll(Collection<Supplier<T>> tasks) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(task, executorService))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    /**
     * Execute multiple tasks in parallel and process results as they complete
     *
     * @param tasks    The tasks to execute
     * @param consumer The consumer to process each result
     * @param <T>      The return type
     * @return A CompletableFuture that completes when all tasks are done
     */
    public <T> CompletableFuture<Void> executeAllAndConsume(Collection<Supplier<T>> tasks,
                                                            Consumer<T> consumer) {

        return CompletableFuture.allOf(
                tasks.stream()
                        .map(task -> CompletableFuture.supplyAsync(task, executorService).thenAccept(consumer))
                        .toArray(CompletableFuture[]::new)
        );
    }

    /**
     * Execute a task with a timeout
     *
     * @param task    The task to execute
     * @param timeout The timeout duration
     * @param unit    The time unit
     * @param <T>     The return type
     * @return A CompletableFuture with the result or a TimeoutException
     */
    public <T> CompletableFuture<T> executeWithTimeout(Supplier<T> task, long timeout, TimeUnit unit) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(task, executorService);

        return future.orTimeout(timeout, unit);
    }

    /**
     * Execute a task with a fallback value if it fails or times out
     *
     * @param task     The task to execute
     * @param fallback The fallback value
     * @param timeout  The timeout duration
     * @param unit     The time unit
     * @param <T>      The return type
     * @return A CompletableFuture with the result or the fallback value
     */
    public <T> CompletableFuture<T> executeWithFallback(Supplier<T> task, T fallback,
                                                        long timeout, TimeUnit unit) {
        return executeWithTimeout(task, timeout, unit)
                .exceptionally(ex -> {
                    log.warn("Task failed or timed out, using fallback value", ex);
                    return fallback;
                });
    }

    /**
     * Execute a task and retry if it fails
     *
     * @param task       The task to execute
     * @param maxRetries The maximum number of retries
     * @param delay      The delay between retries
     * @param unit       The time unit for the delay
     * @param <T>        The return type
     * @return A CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeWithRetry(Supplier<T> task, int maxRetries,
                                                     long delay, TimeUnit unit) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executeWithRetryInternal(task, maxRetries, delay, unit, future, 0);
        return future;
    }

    private <T> void executeWithRetryInternal(Supplier<T> task,
                                              int maxRetries,
                                              long delay,
                                              TimeUnit unit,
                                              CompletableFuture<T> future,
                                              int attempt) {
        CompletableFuture.supplyAsync(task, executorService)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        future.complete(result);
                    } else if (attempt < maxRetries) {
                        log.warn("Attempt {} failed, retrying after delay", attempt + 1, ex);
                        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                        scheduler.schedule(
                                () -> executeWithRetryInternal(task, maxRetries, delay, unit, future, attempt + 1),
                                delay, unit);
                        scheduler.shutdown();
                    } else {
                        log.error("All {} retry attempts failed", maxRetries, ex);
                        future.completeExceptionally(ex);
                    }
                });
    }

    /**
     * Execute a task with a circuit breaker pattern
     *
     * @param task             The task to execute
     * @param fallback         The fallback value if the circuit is open
     * @param failureThreshold The number of failures before opening the circuit
     * @param resetTimeout     The time to wait before trying to close the circuit
     * @param resetUnit        The time unit for the reset timeout
     * @param <T>              The return type
     * @return A CompletableFuture with the result or the fallback value
     */
    public <T> CompletableFuture<T> executeWithCircuitBreaker(Supplier<T> task, T fallback,
                                                              int failureThreshold, long resetTimeout,
                                                              TimeUnit resetUnit) {
        CircuitBreaker<T> circuitBreaker = new CircuitBreaker<>(executorService, failureThreshold, resetTimeout, resetUnit);
        return circuitBreaker.execute(task, fallback);
    }

    /**
     * Execute a task and handle success and failure separately
     *
     * @param task           The task to execute
     * @param successHandler The handler for successful completion
     * @param failureHandler The handler for failures
     * @param <T>            The return type
     * @return A CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeWithHandlers(Supplier<T> task,
                                                        Consumer<T> successHandler,
                                                        Consumer<Throwable> failureHandler) {
        return CompletableFuture.supplyAsync(task, executorService)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        successHandler.accept(result);
                    } else {
                        failureHandler.accept(ex);
                    }
                });
    }

    /**
     * Execute two tasks in sequence, where the second task depends on the result of the first
     *
     * @param firstTask  The first task
     * @param secondTask The second task that takes the result of the first task
     * @param <T>        The return type of the first task
     * @param <R>        The return type of the second task
     * @return A CompletableFuture with the result of the second task
     */
    public <T, R> CompletableFuture<R> executeSequence(Supplier<T> firstTask,
                                                       Function<T, R> secondTask) {
        return CompletableFuture.supplyAsync(firstTask, executorService)
                .thenApplyAsync(secondTask, executorService);
    }

    /**
     * Execute two tasks in parallel and combine their results
     *
     * @param firstTask  The first task
     * @param secondTask The second task
     * @param combiner   The function to combine the results
     * @param <T>        The return type of the first task
     * @param <U>        The return type of the second task
     * @param <R>        The return type after combining
     * @return A CompletableFuture with the combined result
     */
    public <T, U, R> CompletableFuture<R> executeBoth(Supplier<T> firstTask,
                                                      Supplier<U> secondTask,
                                                      BiFunction<T, U, R> combiner) {
        CompletableFuture<T> future1 = CompletableFuture.supplyAsync(firstTask, executorService);
        CompletableFuture<U> future2 = CompletableFuture.supplyAsync(secondTask, executorService);

        return future1.thenCombineAsync(future2, combiner, executorService);
    }

    /**
     * Execute the first task that completes successfully
     *
     * @param tasks The tasks to execute
     * @param <T>   The return type
     * @return A CompletableFuture with the result of the first task to complete
     */
    public <T> CompletableFuture<T> executeAny(Collection<Supplier<T>> tasks) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(task, executorService))
                .collect(Collectors.toList());

        CompletableFuture<T> result = new CompletableFuture<>();

        for (CompletableFuture<T> future : futures) {
            future.whenComplete((value, ex) -> {
                if (ex == null && !result.isDone()) {
                    result.complete(value);
                }
            });
        }

        // If all tasks fail, complete the result exceptionally with the last exception
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    if (!result.isDone()) {
                        result.completeExceptionally(ex);
                    }
                    return null;
                });

        return result;
    }

    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Simple circuit breaker implementation
     */
    private static class CircuitBreaker<T> {
        private final ExecutorService executorService;
        private final int failureThreshold;
        private final long resetTimeout;
        private final TimeUnit resetUnit;

        private int failureCount = 0;
        private boolean circuitOpen = false;
        private long lastFailureTime = 0L;

        public CircuitBreaker(ExecutorService executorService, int failureThreshold,
                              long resetTimeout, TimeUnit resetUnit) {
            this.executorService = executorService;
            this.failureThreshold = failureThreshold;
            this.resetTimeout = resetTimeout;
            this.resetUnit = resetUnit;
        }

        public synchronized CompletableFuture<T> execute(Supplier<T> task, T fallback) {
            // Check if circuit is open
            if (circuitOpen) {
                long currentTime = System.currentTimeMillis();
                long resetMillis = resetUnit.toMillis(resetTimeout);

                if (currentTime - lastFailureTime >= resetMillis) {
                    // Try to close the circuit (half-open state)
                    circuitOpen = false;
                    failureCount = 0;
                    log.info("Circuit breaker reset to closed state");
                } else {
                    // Circuit is still open, return fallback
                    log.debug("Circuit is open, using fallback value");
                    return CompletableFuture.completedFuture(fallback);
                }
            }

            // Circuit is closed or half-open, execute the task
            return CompletableFuture.supplyAsync(task, executorService)
                    .exceptionally(ex -> {
                        // Handle failure
                        synchronized (this) {
                            failureCount++;
                            lastFailureTime = System.currentTimeMillis();

                            if (failureCount >= failureThreshold) {
                                circuitOpen = true;
                                log.warn("Circuit breaker opened after {} failures", failureCount);
                            }
                        }
                        return fallback;
                    });
        }
    }

    /**
     * Execute a task with exponential backoff retry strategy
     *
     * @param task         The task to execute
     * @param maxRetries   The maximum number of retries
     * @param initialDelay The initial delay before the first retry
     * @param maxDelay     The maximum delay between retries
     * @param unit         The time unit for delays
     * @param <T>          The return type
     * @return A CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeWithExponentialBackoff(Supplier<T> task,
                                                                  int maxRetries,
                                                                  long initialDelay,
                                                                  long maxDelay,
                                                                  TimeUnit unit) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executeWithBackoffInternal(task, (double) maxRetries, initialDelay, maxDelay, unit, future, 0d);
        return future;
    }

    private <T> void executeWithBackoffInternal(Supplier<T> task,
                                                double maxRetries,
                                                long initialDelay,
                                                long maxDelay,
                                                TimeUnit unit,
                                                CompletableFuture<T> future,
                                                double attempt) {
        CompletableFuture.supplyAsync(task, executorService)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        future.complete(result);
                    } else if (attempt < maxRetries) {
                        // Calculate exponential backoff delay
                        long delay = Math.min(
                                initialDelay * (long) Math.pow(2.0, attempt),
                                maxDelay
                        );

                        log.warn("Attempt {} failed, retrying after {} ms with exponential backoff",
                                attempt + 1.0, unit.toMillis(delay), ex);

                        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                        scheduler.schedule(
                                () -> executeWithBackoffInternal(
                                        task, maxRetries, initialDelay, maxDelay, unit, future, attempt + 1.0),
                                delay, unit);
                        scheduler.shutdown();
                    } else {
                        log.error("All {} retry attempts with exponential backoff failed", maxRetries, ex);
                        future.completeExceptionally(ex);
                    }
                });
    }

    /**
     * Execute a task with a deadline (absolute timeout)
     *
     * @param task     The task to execute
     * @param deadline The deadline timestamp in milliseconds
     * @param <T>      The return type
     * @return A CompletableFuture with the result or a TimeoutException
     */
    public <T> CompletableFuture<T> executeWithDeadline(Supplier<T> task, long deadline) {
        long timeoutMillis = deadline - System.currentTimeMillis();
        if (timeoutMillis <= 0L) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(new TimeoutException("Deadline already passed"));
            return future;
        }

        return executeWithTimeout(task, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Execute a task and cache the result for a specified duration
     *
     * @param task     The task to execute
     * @param cacheKey The cache key
     * @param duration The cache duration
     * @param unit     The time unit for the duration
     * @param <T>      The return type
     * @return A CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeWithCache(Supplier<T> task,
                                                     Object cacheKey,
                                                     long duration,
                                                     TimeUnit unit) {
        CacheableTask<T> cacheableTask = new CacheableTask<>(executorService, duration, unit);
        return cacheableTask.execute(task, cacheKey);
    }

    /**
     * Execute a task with rate limiting
     *
     * @param task       The task to execute
     * @param permits    The number of permits to acquire
     * @param maxPermits The maximum permits per second
     * @param <T>        The return type
     * @return A CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeWithRateLimit(Supplier<T> task,
                                                         int permits,
                                                         double maxPermits) {
        RateLimitedTask<T> rateLimitedTask = new RateLimitedTask<>(executorService, maxPermits);
        return rateLimitedTask.execute(task, permits);
    }

    /**
     * Execute a task with a bulkhead pattern (limiting concurrent executions)
     *
     * @param task          The task to execute
     * @param maxConcurrent The maximum number of concurrent executions
     * @param maxWaiting    The maximum number of waiting tasks
     * @param <T>           The return type
     * @return A CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeWithBulkhead(Supplier<T> task,
                                                        int maxConcurrent,
                                                        int maxWaiting) {
        BulkheadTask<T> bulkheadTask = new BulkheadTask<>(executorService, maxConcurrent, maxWaiting);
        return bulkheadTask.execute(task);
    }

    /**
     * Execute a task with a fallback function if it fails
     *
     * @param task       The task to execute
     * @param fallbackFn The function to provide a fallback value
     * @param <T>        The return type
     * @return A CompletableFuture with the result or the fallback value
     */
    public <T> CompletableFuture<T> executeWithFallbackFn(Supplier<T> task,
                                                          Function<Throwable, T> fallbackFn) {
        return CompletableFuture.supplyAsync(task, executorService)
                .exceptionally(fallbackFn);
    }

    /**
     * Simple cache implementation for CompletableFuture results
     */
    private static class CacheableTask<T> {
        private final ExecutorService executorService;
        private final long duration;
        private final TimeUnit unit;
        private final ConcurrentMap<Object, CacheEntry<T>> cache = new ConcurrentHashMap<>();

        public CacheableTask(ExecutorService executorService, long duration, TimeUnit unit) {
            this.executorService = executorService;
            this.duration = duration;
            this.unit = unit;
        }

        public CompletableFuture<T> execute(Supplier<T> task, Object cacheKey) {
            // Check if we have a valid cached result
            CacheEntry<T> entry = cache.get(cacheKey);
            if (entry != null && !entry.isExpired()) {
                return CompletableFuture.completedFuture(entry.getValue());
            }

            // Execute the task and cache the result
            return CompletableFuture.supplyAsync(task, executorService)
                    .thenApply(result -> {
                        cache.put(cacheKey, new CacheEntry<>(result,
                                System.currentTimeMillis() + unit.toMillis(duration)));
                        return result;
                    });
        }

        private static class CacheEntry<T> {
            @Getter
            private final T value;
            private final long expiryTime;

            public CacheEntry(T value, long expiryTime) {
                this.value = value;
                this.expiryTime = expiryTime;
            }

            public boolean isExpired() {
                return System.currentTimeMillis() > expiryTime;
            }
        }
    }

    /**
     * Rate limiter implementation for CompletableFuture tasks
     */
    private static class RateLimitedTask<T> {
        private final ExecutorService executorService;
        private final RateLimiter rateLimiter;

        public RateLimitedTask(ExecutorService executorService, double permitsPerSecond) {
            this.executorService = executorService;
            this.rateLimiter = new RateLimiter(permitsPerSecond);
        }

        public CompletableFuture<T> execute(Supplier<T> task, int permits) {
            CompletableFuture<T> future = new CompletableFuture<>();

            // Try to acquire permits
            CompletableFuture.runAsync(() -> {
                try {
                    rateLimiter.acquire(permits);

                    // Execute the task after acquiring permits
                    CompletableFuture.supplyAsync(task, executorService)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    future.completeExceptionally(ex);
                                } else {
                                    future.complete(result);
                                }
                            });
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });

            return future;
        }
    }

    /**
     * Simple rate limiter implementation
     */
    private static class RateLimiter {
        private final double permitsPerSecond;
        private double storedPermits;
        private long lastRefillTime;

        public RateLimiter(double permitsPerSecond) {
            this.permitsPerSecond = permitsPerSecond;
            this.storedPermits = 0.0;
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized void acquire(double permits) throws InterruptedException {
            refillPermits();

            // Calculate wait time if not enough permits
            long waitTimeMillis = 0L;
            if (permits > storedPermits) {
                double missingPermits = permits - storedPermits;
                waitTimeMillis = (long) (1000.0 * missingPermits / permitsPerSecond);
            }

            if (waitTimeMillis > 0L) {
                Thread.sleep(waitTimeMillis);
                refillPermits();
            }

            storedPermits -= permits;
        }

        private void refillPermits() {
            long now = System.currentTimeMillis();
            double newPermits = (double) (now - lastRefillTime) / 1000.0 * permitsPerSecond;
            storedPermits = Math.min(permitsPerSecond, storedPermits + newPermits);
            lastRefillTime = now;
        }
    }

    /**
     * Bulkhead implementation for CompletableFuture tasks
     */
    private static class BulkheadTask<T> {
        private final ExecutorService executorService;
        private final Semaphore executionSemaphore;
        private final Queue<Runnable> waitingQueue = new ConcurrentLinkedQueue<>();
        private final int maxWaiting;
        private final AtomicLong waitingCount = new AtomicLong(0L);

        public BulkheadTask(ExecutorService executorService, int maxConcurrent, int maxWaiting) {
            this.executorService = executorService;
            this.executionSemaphore = new Semaphore(maxConcurrent);
            this.maxWaiting = maxWaiting;
        }

        public CompletableFuture<T> execute(Supplier<T> task) {
            CompletableFuture<T> future = new CompletableFuture<>();

            // Check if we can add to waiting queue
            if (waitingCount.incrementAndGet() > (long) maxWaiting) {
                waitingCount.decrementAndGet();
                future.completeExceptionally(new RejectedExecutionException(
                        "Bulkhead capacity exceeded: " + maxWaiting + " tasks waiting"));
                return future;
            }

            // Add to waiting queue
            waitingQueue.offer(() -> {
                try {
                    // Try to acquire a permit
                    if (!executionSemaphore.tryAcquire(5L, TimeUnit.SECONDS)) {
                        future.completeExceptionally(new TimeoutException(
                                "Timeout waiting for bulkhead execution slot"));
                        return;
                    }

                    try {
                        // Execute the task
                        CompletableFuture.supplyAsync(task, executorService)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        future.completeExceptionally(ex);
                                    } else {
                                        future.complete(result);
                                    }

                                    // Release the permit
                                    executionSemaphore.release();

                                    // Process next waiting task if any
                                    processNextWaitingTask();
                                });
                    } catch (Exception e) {
                        executionSemaphore.release();
                        future.completeExceptionally(e);
                        processNextWaitingTask();
                    }
                } catch (InterruptedException e) {
                    future.completeExceptionally(e);
                }
            });

            // Try to process immediately
            processNextWaitingTask();

            return future;
        }

        private void processNextWaitingTask() {
            Runnable nextTask = waitingQueue.poll();
            if (nextTask != null) {
                waitingCount.decrementAndGet();
                CompletableFuture.runAsync(nextTask);
            }
        }
    }
}