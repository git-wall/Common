package org.app.common.test.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

// Arrange/Act/Assert pattern implementation for unit testing.
public class AAA<T, R> {
    private T arrange;
    private Function<T, R> action;
    private List<Predicate<R>> assertThat;

    public static <T, R> AAA<T, R> of() {
        return new AAA<>();
    }

    public AAA<T, R> arrange(T arrange) {
        this.arrange = arrange;
        return this;
    }

    public AAA<T, R> action(Function<T, R> action) {
        this.action = action;
        return this;
    }

    public AAA<T, R> assertThat(Predicate<R> assertThat) {
        if (this.assertThat == null) this.assertThat = new ArrayList<>();
        this.assertThat.add(assertThat);
        return this;
    }


    public boolean execute() {
        try {
            T context = arrange;

            R result = action.apply(context);

            return assertThat.stream().allMatch(assertion -> assertion.test(result));
        } catch (Exception e) {
            return false;
        }
    }
}