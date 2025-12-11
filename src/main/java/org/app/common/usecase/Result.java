package org.app.common.usecase;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Result<T> {
    private final T value;
    private final Throwable error;

    private Result(T value, Throwable error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> ok(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> fail(Throwable error) {
        return new Result<>(null, error);
    }

    public boolean isOk() {
        return error == null;
    }

    public T get() {
        if (error != null) throw new RuntimeException(error);
        return value;
    }

    // ---------------- Basic map / flatMap ----------------

    public <R> Result<R> map(Function<T, R> mapper) {
        if (!isOk()) return fail(error);
        try {
            return ok(mapper.apply(value));
        } catch (Exception e) {
            return fail(e);
        }
    }

    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (!isOk()) return fail(error);
        try {
            return mapper.apply(value);
        } catch (Exception e) {
            return fail(e);
        }
    }

    // ---------------- Validation ----------------
    public Result<T> validate(Consumer<T> validator) {
        if (!isOk()) return this;
        try {
            validator.accept(value);
            return this;
        } catch (Exception e) {
            return fail(e);
        }
    }

    public Result<T> validateIf(Predicate<T> condition, Consumer<T> validator) {
        if (!isOk()) return this;
        try {
            if (condition.test(value)) {
                validator.accept(value);
            }
            return this;
        } catch (Exception e) {
            return fail(e);
        }
    }

    // ---------------- Conditional map / flatMap ----------------
    public <R> Result<R> mapIf(Predicate<T> condition, Function<T, R> mapper, R defaultValue) {
        if (!isOk()) return fail(error);
        try {
            if (condition.test(value)) {
                return ok(mapper.apply(value));
            } else {
                return ok(defaultValue);
            }
        } catch (Exception e) {
            return fail(e);
        }
    }

    public <R> Result<R> flatMapIf(Predicate<T> condition, Function<T, Result<R>> mapper, Result<R> defaultResult) {
        if (!isOk()) return fail(error);
        try {
            if (condition.test(value)) {
                return mapper.apply(value);
            } else {
                return defaultResult;
            }
        } catch (Exception e) {
            return fail(e);
        }
    }

    // ---------------- Error handling ----------------
    public Result<T> throwIfError() {
        if (error != null) {
            if (error instanceof RuntimeException) throw (RuntimeException) error;
            else throw new RuntimeException(error);
        }
        return this;
    }

    public <R> R orElse(Function<Throwable, R> onError) {
        return isOk() ? (R) value : onError.apply(error);
    }

    public T orElse(T defaultValue) {
        return isOk() ? value : defaultValue;
    }

    public void ifOk(Consumer<T> consumer) {
        if (isOk()) consumer.accept(value);
    }

    public void ifFail(Consumer<Throwable> consumer) {
        if (!isOk()) consumer.accept(error);
    }
}

