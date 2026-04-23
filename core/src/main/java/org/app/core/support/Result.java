package org.app.core.support;

import lombok.Getter;
import org.app.exception.VerifyException;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Result<T> — wrapper cho success/failure, chain-friendly, tích hợp Verify.
 * <p>
 * Dùng Verify bên trong validate() để kiểm tra data sau khi lấy về:
 * <pre>{@code
 *   Result.of(() -> repo.findById(id))
 *       .validate(user -> Verify.ifNull(user.getEmail(), "Email required"))
 *       .validate(user -> Verify.ifTrue(user.isBanned(), "User is banned"))
 *       .flatMap(user -> fetchOrder(user))
 *       .onSuccess(order -> ...)
 *       .onFailure(err -> log.error(err.getMessage()));
 * }</pre>
 */
public final class Result<T> {

    private final T value;
    @Getter
    private final Throwable error;

    private Result(T value, Throwable error) {
        this.value = value;
        this.error = error;
    }

    // ── Tạo ──────────────────────────────────────────────────────

    public static <T> Result<T> ok(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> fail(Throwable error) {
        return new Result<>(null, error);
    }

    public static <T> Result<T> of(ThrowingSupplier<T> supplier) {
        try {
            return ok(supplier.get());
        } catch (Throwable e) {
            return fail(e);
        }
    }

    /** Từ Optional — empty → fail từ supplier exception */
    public static <T, E extends Throwable> Result<T> of(T data, Supplier<E> ifEmpty) {
        return data != null ? Result.ok(data) : fail(ifEmpty.get());
    }

    // ── Trạng thái ───────────────────────────────────────────────

    public boolean isOk()   { return error == null; }
    public boolean isFail() { return error != null; }

    // ── Lấy giá trị ──────────────────────────────────────────────

    /** Lấy value, throw nếu fail */
    public T get() {
        if (isFail()) throw new VerifyException("Result failed: " + error.getMessage());
        return value;
    }

    public T getOrDefault(T defaultValue)     { return isOk() ? value : defaultValue; }
    public T getOrElse(Supplier<T> supplier)  { return isOk() ? value : supplier.get(); }
    public Optional<T> toOptional()           { return isOk() ? Optional.ofNullable(value) : Optional.empty(); }
    public String getErrorMessage()           { return isFail() ? error.getMessage() : null; }

    // ── Transform ────────────────────────────────────────────────

    public <R> Result<R> map(Function<T, R> mapper) {
        if (isFail()) return fail(error);
        try { return ok(mapper.apply(value)); }
        catch (Throwable e) { return fail(e); }
    }

    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (isFail()) return fail(error);
        try { return mapper.apply(value); }
        catch (Throwable e) { return fail(e); }
    }

    /** map chỉ khi condition đúng, không đổi type */
    public Result<T> mapIf(Predicate<T> condition, Function<T, T> mapper) {
        if (isFail()) return this;
        try { return condition.test(value) ? ok(mapper.apply(value)) : this; }
        catch (Throwable e) { return fail(e); }
    }

    /** flatMap chỉ khi condition đúng, giữ nguyên nếu không */
    public Result<T> flatMapIf(Predicate<T> condition, Function<T, Result<T>> mapper) {
        if (isFail()) return this;
        try { return condition.test(value) ? mapper.apply(value) : this; }
        catch (Throwable e) { return fail(e); }
    }

    // ── Validate — dùng Verify.* bên trong consumer ──────────────

    /**
     * Chạy validator trên value. Nếu Verify throw VerifyException → Result.fail.
     * <pre>
     *   .validate(u -> Verify.ifNull(u.getName(), "Name required"))
     *   .validate(u -> Verify.ifTrue(u.getAge() < 0, "Invalid age"))
     *   .validate(u -> Verify.requireBetween(u.getAge(), 0, 120, "Age out of range"))
     * </pre>
     */
    public Result<T> validate(Consumer<T> validator) {
        if (isFail()) return this;
        try { validator.accept(value); return this; }
        catch (Throwable e) { return fail(e); }
    }

    /** validate chỉ khi condition đúng */
    public Result<T> validateIf(Predicate<T> condition, Consumer<T> validator) {
        if (isFail()) return this;
        try {
            if (condition.test(value)) validator.accept(value);
            return this;
        } catch (Throwable e) { return fail(e); }
    }

    // ── Recovery ─────────────────────────────────────────────────

    /** Nếu fail, thử lại bằng Result khác */
    public Result<T> recover(Function<Throwable, Result<T>> recovery) {
        if (isOk()) return this;
        try { return recovery.apply(error); }
        catch (Throwable e) { return fail(e); }
    }

    /** Nếu fail và match đúng type exception, thử lại */
    @SuppressWarnings("unchecked")
    public <E extends Throwable> Result<T> recoverIf(Class<E> errorType, Function<E, Result<T>> recovery) {
        if (isOk() || !errorType.isInstance(error)) return this;
        try { return recovery.apply((E) error); }
        catch (Throwable e) { return fail(e); }
    }

    /** Nếu fail, trả về Result khác từ supplier */
    public Result<T> orElse(Supplier<Result<T>> supplier) {
        return isOk() ? this : supplier.get();
    }

    // ── Side effects ──────────────────────────────────────────────

    public Result<T> onSuccess(Consumer<T> action) {
        if (isOk()) action.accept(value);
        return this;
    }

    public Result<T> onFailure(Consumer<Throwable> action) {
        if (isFail()) action.accept(error);
        return this;
    }

    /** Peek — xem value giữa chain mà không transform, tiện log */
    public Result<T> peek(Consumer<T> action) {
        if (isOk()) action.accept(value);
        return this;
    }

    /** Throw nếu fail — dùng cuối chain khi muốn propagate ra ngoài */
    public Result<T> throwIfFail() {
        if (isFail()) {
            if (error instanceof RuntimeException)
                throw (RuntimeException) error;
            throw new VerifyException(error.getMessage());
        }
        return this;
    }

    // ── Helper types ──────────────────────────────────────────────

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    @Override
    public String toString() {
        return isOk() ? "Result.ok(" + value + ")" : "Result.fail(" + error.getMessage() + ")";
    }
}
