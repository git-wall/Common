package org.app.common.validation;

import io.vavr.Tuple2;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class Valid2<T1, T2> {
    private final Tuple2<T1, T2> ctx;
    private final List<String> errors = new ArrayList<>();
    private Mode mode = Mode.COLLECT_ALL;

    public Valid2(T1 t1, T2 t2) {
        this.ctx = new Tuple2<>(t1, t2);
    }

    public static <T1, T2> Valid2<T1, T2> of(T1 t1, T2 t2) {
        return new Valid2<>(t1, t2);
    }

    public Valid2<T1, T2> failFast() {
        this.mode = Mode.FAIL_FAST;
        return this;
    }

    // ========== BASIC CHECKS ==========

    public Valid2<T1, T2> check(String message, Predicate<Tuple2<T1, T2>> predicate) {
        if (!predicate.test(ctx)) {
            addError(message);
        }
        return this;
    }

    public <U> Valid2<T1, T2> check(String message,
                                    Function<Tuple2<T1, T2>, ? extends U> fn,
                                    Predicate<? super U> pred) {
        check(message, fn.andThen(pred::test)::apply);
        return this;
    }

    public <A, B> Valid2<T1, T2> check(
        String message,
        Function<T1, A> leftFn,
        Function<T2, B> rightFn,
        BiPredicate<A, B> pred) {

        A left = leftFn.apply(ctx._1);
        B right = rightFn.apply(ctx._2);

        if (!pred.test(left, right)) {
            addError(message);
        }

        return this;
    }

    // ========== SHORTCUT METHODS ==========

    public Valid2<T1, T2> notNull(String message, Function<Tuple2<T1, T2>, ?> field) {
        return check(message, field, Objects::nonNull);
    }

    public <U> Valid2<T1, T2> equal(String message, Function<T1, U> left, Function<T2, U> right) {
        return check(message, left, right, Objects::equals);
    }

    public <U> Valid2<T1, T2> in(String msg, Function<Tuple2<T1, T2>, U> getter, Collection<U> allowed) {
        U v = getter.apply(ctx);
        if (!allowed.contains(v)) addError(msg);
        return this;
    }

    public Valid2<T1, T2> notBlank(String msg, Function<Tuple2<T1, T2>, String> getter) {
        var v = getter.apply(ctx);
        if (v == null || v.trim().isEmpty()) addError(msg);
        return this;
    }

    // ========== FINALIZE ==========

    public void validate() {
        if (!errors.isEmpty()) {
            IllegalStateException ex = new IllegalStateException("Validation failed");
            errors.forEach(msg -> ex.addSuppressed(new IllegalStateException(msg)));
            throw ex;
        }
    }

    private void addError(String msg) {
        if (mode == Mode.FAIL_FAST) {
            throw new IllegalStateException(msg);
        }
        errors.add(msg);
    }

    public enum Mode {
        FAIL_FAST,
        COLLECT_ALL
    }
}
