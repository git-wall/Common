package org.app.core.action;

import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Railway Oriented Programming — Result<T, E>
 *
 * Thay thế cho throw/null. Mọi bước trong pipeline đều trả về
 * Result, caller quyết định xử lý lỗi chứ không bị surprise exception.
 *
 * Usage:
 *   Result<User, AppError> r = Result.ok(user);
 *   r.map(User::getName)
 *    .flatMap(name -> callApi(name))
 *    .onSuccess(resp -> saveDb(resp))
 *    .onFailure(err -> log.error(err.message()));
 */
public interface Result<T, E> {

    // ─── Constructors ───────────────────────────────────────────────────────

    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }

    /** Bắt exception, convert thành Err tự động */
    static <T, E> Result<T, E> tryGet(Supplier<T> supplier, Function<Exception, E> onException) {
        try {
            return ok(supplier.get());
        } catch (Exception ex) {
            return err(onException.apply(ex));
        }
    }

    // ─── Core checks ────────────────────────────────────────────────────────

    boolean isOk();
    boolean isErr();

    Optional<T> value();
    Optional<E> error();

    T getOrThrow();
    T getOrElse(T fallback);
    T getOrElse(Supplier<T> fallbackSupplier);

    // ─── Transform value (không thay đổi nếu đang là Err) ──────────────────

    /** map: T → U, nếu Ok thì transform, nếu Err thì pass through */
    <U> Result<U, E> map(Function<T, U> mapper);

    /** flatMap: T → Result<U,E>, dùng khi step tiếp theo cũng có thể fail */
    <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper);

    /** mapError: transform error type */
    <F> Result<T, F> mapError(Function<E, F> errorMapper);

    // ─── Side effects (không thay đổi Result, chỉ "tap") ────────────────────

    /** Chạy consumer nếu Ok, trả về chính nó — dùng cho save, notify, log */
    Result<T, E> onSuccess(Consumer<T> action);

    /** Chạy consumer nếu Err, trả về chính nó */
    Result<T, E> onFailure(Consumer<E> action);

    /** Chạy trong background (fire & forget), không block chain */
    Result<T, E> onSuccessAsync(Consumer<T> action);
    Result<T, E> onFailureAsync(Consumer<E> action);

    /** Luôn chạy dù Ok hay Err — dùng cho cleanup, metrics */
    Result<T, E> peek(Consumer<Result<T, E>> observer);

    // ─── Recovery ───────────────────────────────────────────────────────────

    /** Nếu Err, thử recover thành Ok */
    Result<T, E> recover(Function<E, T> recovery);

    /** Nếu Err, thử recover bằng một Result khác */
    Result<T, E> recoverWith(Function<E, Result<T, E>> recovery);

    // ─── Implementations ────────────────────────────────────────────────────

    @AllArgsConstructor
    class Ok<T, E> implements Result<T, E> {
        T val;
        @Override public boolean isOk()  { return true; }
        @Override public boolean isErr() { return false; }
        @Override public Optional<T> value() { return Optional.ofNullable(val); }
        @Override public Optional<E> error() { return Optional.empty(); }
        @Override public T getOrThrow()             { return val; }
        @Override public T getOrElse(T fallback)    { return val; }
        @Override public T getOrElse(Supplier<T> s) { return val; }

        @Override
        public <U> Result<U, E> map(Function<T, U> mapper) {
            try {
                return ok(mapper.apply(val));
            } catch (Exception ex) {
                throw new PipelineException("map() threw unexpectedly — use flatMap() for fallible steps", ex);
            }
        }

        @Override
        public <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
            return mapper.apply(val);
        }

        @Override
        public <F> Result<T, F> mapError(Function<E, F> errorMapper) {
            return Result.ok(val);
        }

        @Override
        public Result<T, E> onSuccess(Consumer<T> action) {
            action.accept(val);
            return this;
        }

        @Override
        public Result<T, E> onFailure(Consumer<E> action) {
            return this; // no-op
        }

        @Override
        public Result<T, E> onSuccessAsync(Consumer<T> action) {
            CompletableFuture.runAsync(() -> action.accept(val));
            return this;
        }

        @Override
        public Result<T, E> onFailureAsync(Consumer<E> action) {
            return this;
        }

        @Override
        public Result<T, E> peek(Consumer<Result<T, E>> observer) {
            observer.accept(this);
            return this;
        }

        @Override
        public Result<T, E> recover(Function<E, T> recovery) {
            return this;
        }

        @Override
        public Result<T, E> recoverWith(Function<E, Result<T, E>> recovery) {
            return this;
        }

        @Override public String toString() { return "Ok(" + val + ")"; }
    }

    @AllArgsConstructor
    class Err<T, E> implements Result<T, E> {
        E err;
        @Override public boolean isOk()  { return false; }
        @Override public boolean isErr() { return true; }
        @Override public Optional<T> value() { return Optional.empty(); }
        @Override public Optional<E> error() { return Optional.ofNullable(err); }

        @Override
        public T getOrThrow() {
            throw new PipelineException("Called getOrThrow() on Err: " + err);
        }

        @Override public T getOrElse(T fallback)    { return fallback; }
        @Override public T getOrElse(Supplier<T> s) { return s.get(); }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U, E> map(Function<T, U> mapper) {
            return (Result<U, E>) this; // propagate error, skip mapper
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
            return (Result<U, E>) this;
        }

        @Override
        public <F> Result<T, F> mapError(Function<E, F> errorMapper) {
            return Result.err(errorMapper.apply(err));
        }

        @Override
        public Result<T, E> onSuccess(Consumer<T> action) {
            return this;
        }

        @Override
        public Result<T, E> onFailure(Consumer<E> action) {
            action.accept(err);
            return this;
        }

        @Override
        public Result<T, E> onSuccessAsync(Consumer<T> action) {
            return this;
        }

        @Override
        public Result<T, E> onFailureAsync(Consumer<E> action) {
            CompletableFuture.runAsync(() -> action.accept(err));
            return this;
        }

        @Override
        public Result<T, E> peek(Consumer<Result<T, E>> observer) {
            observer.accept(this);
            return this;
        }

        @Override
        public Result<T, E> recover(Function<E, T> recovery) {
            return ok(recovery.apply(err));
        }

        @Override
        public Result<T, E> recoverWith(Function<E, Result<T, E>> recovery) {
            return recovery.apply(err);
        }

        @Override public String toString() { return "Err(" + err + ")"; }
    }

    class PipelineException extends RuntimeException {
        public PipelineException(String msg) { super(msg); }
        public PipelineException(String msg, Throwable cause) { super(msg, cause); }
    }
}
