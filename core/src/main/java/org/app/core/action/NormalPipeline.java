package org.app.core.action;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * NormalPipeline — dành cho data nhỏ (JSON API, DB record, event payload...)
 *
 * Thiết kế: immutable chain, mỗi step trả về NormalPipeline mới.
 * Fan-out handlers (onSuccess/onFailure) chạy tất cả, độc lập nhau.
 *
 * Use case điển hình:
 *   NormalPipeline.from(() -> fetchUser(id))   // source
 *     .map(User::toDto)                         // transform
 *     .flatMap(dto -> validateDto(dto))          // fallible step
 *     .flatMap(dto -> callExternalApi(dto))      // API call
 *     .onSuccess(resp -> saveToDb(resp))         // blocking side-effect
 *     .onSuccessAsync(resp -> notify(resp))      // fire & forget
 *     .onFailure(err -> logError(err))           // error handler
 *     .execute();                                // kick off
 */
public class NormalPipeline<T, E> {

    private final Supplier<Result<T, E>> computation;
    private final List<Consumer<T>> successHandlers = new ArrayList<>();
    private final List<Consumer<T>> asyncSuccessHandlers = new ArrayList<>();
    private final List<Consumer<E>> failureHandlers = new ArrayList<>();
    private final List<Consumer<E>> asyncFailureHandlers = new ArrayList<>();
    private final List<Consumer<Result<T, E>>> peekHandlers = new ArrayList<>();

    private NormalPipeline(Supplier<Result<T, E>> computation) {
        this.computation = computation;
    }

    // ─── Entry points ───────────────────────────────────────────────────────

    /** Tạo pipeline từ một Result trực tiếp */
    public static <T, E> NormalPipeline<T, E> of(Result<T, E> result) {
        return new NormalPipeline<>(() -> result);
    }

    /** Tạo pipeline từ một supplier có thể fail */
    public static <T, E> NormalPipeline<T, E> from(Supplier<Result<T, E>> supplier) {
        return new NormalPipeline<>(supplier);
    }

    /** Bắt exception tự động, không cần tự wrap try-catch */
    public static <T, E> NormalPipeline<T, E> tryFrom(
            Supplier<T> supplier,
            Function<Exception, E> errorMapper
    ) {
        return new NormalPipeline<>(() -> Result.tryGet(supplier, errorMapper));
    }

    // ─── Transformations ────────────────────────────────────────────────────

    /**
     * map: transform T → U nếu Ok, skip nếu Err.
     * Dùng cho các bước chắc chắn không fail (parse, convert, format).
     */
    public <U> NormalPipeline<U, E> map(Function<T, U> mapper) {
        return new NormalPipeline<>(() -> computation.get().map(mapper));
    }

    /**
     * flatMap: T → Result<U,E>, dùng khi bước tiếp theo CÓ THỂ fail.
     * Ví dụ: gọi API, query DB, validate.
     *
     *   pipeline.flatMap(user -> {
     *       var resp = callApi(user);  // có thể fail
     *       return resp.isOk() ? Result.ok(resp.body()) : Result.err(AppError.API_FAIL);
     *   });
     */
    public <U> NormalPipeline<U, E> flatMap(Function<T, Result<U, E>> mapper) {
        return new NormalPipeline<>(() -> computation.get().flatMap(mapper));
    }

    /**
     * filter: nếu Ok nhưng không pass predicate → chuyển thành Err.
     */
    public NormalPipeline<T, E> filter(Predicate<T> predicate, Supplier<E> errorIfFails) {
        return new NormalPipeline<>(() -> {
            Result<T, E> r = computation.get();
            if (r.isOk() && !predicate.test(r.getOrThrow())) {
                return Result.err(errorIfFails.get());
            }
            return r;
        });
    }

    // ─── Fan-out handlers ────────────────────────────────────────────────────

    /**
     * onSuccess (blocking): chạy action và CHỜ xong trước khi tiếp.
     * Dùng cho: save DB (cần biết thành công/thất bại), ghi audit log.
     */
    public NormalPipeline<T, E> onSuccess(Consumer<T> action) {
        NormalPipeline<T, E> next = copy();
        next.successHandlers.add(action);
        return next;
    }

    /**
     * onSuccessAsync (fire & forget): chạy trong virtual thread, KHÔNG block chain.
     * Dùng cho: push notification, email, analytics, cache invalidate.
     * Lưu ý: không capture exception từ đây — dùng riêng error handling nội bộ.
     */
    public NormalPipeline<T, E> onSuccessAsync(Consumer<T> action) {
        NormalPipeline<T, E> next = copy();
        next.asyncSuccessHandlers.add(action);
        return next;
    }

    /**
     * onFailure (blocking): chạy khi Err, CHỜ xong.
     * Dùng cho: structured logging với context, rollback operations.
     */
    public NormalPipeline<T, E> onFailure(Consumer<E> action) {
        NormalPipeline<T, E> next = copy();
        next.failureHandlers.add(action);
        return next;
    }

    /**
     * onFailureAsync (fire & forget): chạy khi Err, không block.
     * Dùng cho: alert/pager duty, error tracking (Sentry, Datadog).
     */
    public NormalPipeline<T, E> onFailureAsync(Consumer<E> action) {
        NormalPipeline<T, E> next = copy();
        next.asyncFailureHandlers.add(action);
        return next;
    }

    /**
     * peek: luôn chạy dù Ok hay Err — dùng cho metrics, tracing.
     */
    public NormalPipeline<T, E> peek(Consumer<Result<T, E>> observer) {
        NormalPipeline<T, E> next = copy();
        next.peekHandlers.add(observer);
        return next;
    }

    // ─── Recovery ────────────────────────────────────────────────────────────

    /** Nếu fail, thử phục hồi bằng fallback value */
    public NormalPipeline<T, E> recover(Function<E, T> recovery) {
        return new NormalPipeline<>(() -> computation.get().recover(recovery));
    }

    /** Nếu fail, thử lại bằng một pipeline khác (circuit breaker pattern) */
    public NormalPipeline<T, E> fallback(Supplier<Result<T, E>> fallbackSupplier) {
        return new NormalPipeline<>(() -> {
            Result<T, E> r = computation.get();
            return r.isErr() ? fallbackSupplier.get() : r;
        });
    }

    // ─── Execute ─────────────────────────────────────────────────────────────

    /**
     * execute(): chạy toàn bộ pipeline, chờ blocking handlers,
     * kick off async handlers (không chờ), trả về Result ngay.
     *
     * Caller nhận Result → có thể làm thêm gì đó hoặc return cho controller.
     */
    public Result<T, E> execute() {
        Result<T, E> result = computation.get();

        // Peek handlers: luôn chạy
        peekHandlers.forEach(h -> safeRun(() -> h.accept(result)));

        if (result.isOk()) {
            T value = result.getOrThrow();

            // Blocking success handlers (theo thứ tự)
            for (Consumer<T> handler : successHandlers) {
                safeRun(() -> handler.accept(value));
            }

            // Async success handlers (tất cả cùng lúc, không chờ)
            if (!asyncSuccessHandlers.isEmpty()) {
                asyncSuccessHandlers.stream()
                    .map(h -> CompletableFuture.runAsync(() -> safeRun(() -> h.accept(value))))
                    .collect(Collectors.toList());
                // Không join — intentionally fire & forget
            }
        } else {
            E error = result.error().orElseThrow();

            // Blocking failure handlers
            for (Consumer<E> handler : failureHandlers) {
                safeRun(() -> handler.accept(error));
            }

            // Async failure handlers
            if (!asyncFailureHandlers.isEmpty()) {
                asyncFailureHandlers.forEach(h ->
                    CompletableFuture.runAsync(() -> safeRun(() -> h.accept(error)))
                );
            }
        }

        return result;
    }

    /**
     * executeAsync(): toàn bộ pipeline chạy trong CompletableFuture.
     * Dùng khi bản thân pipeline này cần chạy non-blocking từ caller.
     */
    public CompletableFuture<Result<T, E>> executeAsync() {
        return CompletableFuture.supplyAsync(this::execute);
    }

    // ─── Internals ────────────────────────────────────────────────────────────

    private NormalPipeline<T, E> copy() {
        NormalPipeline<T, E> next = new NormalPipeline<>(this.computation);
        next.successHandlers.addAll(this.successHandlers);
        next.asyncSuccessHandlers.addAll(this.asyncSuccessHandlers);
        next.failureHandlers.addAll(this.failureHandlers);
        next.asyncFailureHandlers.addAll(this.asyncFailureHandlers);
        next.peekHandlers.addAll(this.peekHandlers);
        return next;
    }

    private void safeRun(Runnable r) {
        try {
            r.run();
        } catch (Exception ex) {
            // Handler tự throw — không được crash pipeline chính.
            // Trong production: log ra structured logger ở đây.
            System.err.println("[Pipeline] Handler threw: " + ex.getMessage());
        }
    }
}
