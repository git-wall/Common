package org.app.core.patterns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Composable Saga Orchestrator.
 * Implements the Saga pattern for distributed transactions WITHOUT 2PC.
 * Each step has a forward action and a compensation action.
 * If any step fails, all previously completed steps are compensated in reverse order.
 *
 * <pre>
 *     State machine:  INIT → RESERVED → CONFIRMING → COMPLETED
 *                                 ↘           ↘
 *                                    CANCELLED</pre>
 * <pre>{@code
 *   SagaOrchestrator.of("confirm-payment")
 *       .step("reserve-inventory",
 *             () -> inventory.reserve(...),
 *             () -> inventory.release(...))
 *       .step("create-order",
 *             () -> orderService.create(...),
 *             () -> orderService.cancel(...))
 *       .step("charge-payment",
 *             () -> paymentGateway.charge(...),
 *             () -> paymentGateway.refund(...))
 *       .execute();
 * }</pre>
 * Usage:
 * Design decisions: <br>
 * - Compensations are stored and run in LIFO order (last completed = first compensated) <br>
 * - Each compensation is wrapped in try/catch so one failing compensation
 *   doesn't block others (best-effort compensation, log failures for manual review) <br>
 * - SagaResult carries the failure reason for structured error handling
 */
public class SagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrator.class);

    public static SagaBuilder of(String sagaName) {
        return new SagaBuilder(sagaName);
    }

    // ── Builder ────────────────────────────────────────────────────────────

    public static class SagaBuilder {
        private final String name;
        private final List<SagaStep> steps = new ArrayList<>();

        SagaBuilder(String name) { this.name = name; }

        /**
         * Add a step with forward action and compensation.
         *
         * @param stepName     human-readable name for logs/traces
         * @param action       forward action — throws on failure
         * @param compensation rollback action — should be idempotent
         */
        public SagaBuilder step(String stepName, Runnable action, Runnable compensation) {
            // SagaStep holds (name, action, compensation). Don't pass saga name here.
            steps.add(new SagaStep(stepName, action, compensation));
            return this;
        }

        /** Step with return value capture (e.g. save order ID for compensation) */
        public <T> SagaBuilder step(String stepName,
                                    Supplier<T> action,
                                    Consumer<T> compensation,
                                    SagaContext ctx,
                                    String resultKey) {
            steps.add(new SagaStep(stepName,
                () -> ctx.put(resultKey, action.get()),
                () -> compensation.accept(ctx.get(resultKey))));
            return this;
        }

        /** Step with return value capture (e.g. save order ID for compensation) */
        public <T> SagaBuilder step(String stepName,
                                    Supplier<T> action,
                                    SagaContext ctx,
                                    String resultKey) {
            steps.add(new SagaStep(stepName, () -> ctx.put(resultKey, action.get()), null)); // No compensation provided
            return this;
        }

        public SagaResult execute() {
            Deque<SagaStep> completed = new ArrayDeque<>();

            for (SagaStep step : steps) {
                try {
                    log.info("[Saga:{}] Executing step: {}", name, step.getName());
                    step.getAction().run();
                    completed.push(step);
                } catch (Exception ex) {
                    log.error("[Saga:{}] Step '{}' failed: {}", name, step.getName(), ex.getMessage());

                    // Compensate in reverse order
                    compensate(name, completed);

                    return SagaResult.failure(step.getName(), ex);
                }
            }

            return SagaResult.success();
        }

        private void compensate(String sagaName, Deque<SagaStep> completed) {
            log.warn("[Saga:{}] Starting compensation for {} steps", sagaName, completed.size());
            while (!completed.isEmpty()) {
                SagaStep step = completed.pop();
                try {
                    log.info("[Saga:{}] Compensating: {}", sagaName, step.getName());
                    var compensation = step.getCompensation();
                    if (compensation != null) {
                        compensation.run();
                    }
                } catch (Exception ex) {
                    // Compensation failure is logged but does not block others.
                    // These must be handled by a reconciliation job / dead-letter queue.
                    log.error("[Saga:{}] COMPENSATION FAILED for '{}': {} — add to DLQ",
                        sagaName, step.getName(), ex.getMessage());
                }
            }
        }
    }

    // ── Supporting types ────────────────────────────────────────────────────
    @Data
    @AllArgsConstructor
    public static class SagaStep {
        private final String name;
        private final Runnable action;
        private final Runnable compensation;
    }

    /** Carry-over context between steps */
    public static class SagaContext {
        private final Map<String, Object> store = new HashMap<>();
        public void put(String key, Object val) { store.put(key, val); }
        @SuppressWarnings("unchecked")
        public <T> T get(String key) { return (T) store.get(key); }
    }

    @Getter
    public static class SagaResult {
        private final boolean success;
        private final String failedStep;
        private final Exception cause;

        private SagaResult(boolean success, String failedStep, Exception cause) {
            this.success = success;
            this.failedStep = failedStep;
            this.cause = cause;
        }

        static SagaResult success() { return new SagaResult(true, null, null); }
        static SagaResult failure(String step, Exception ex) {
            return new SagaResult(false, step, ex);
        }
    }
}
