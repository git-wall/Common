package org.app.common.test.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

// TDD, BDD
public class GWT<T,R> {
    private Runnable given;
    private T context;
    private Function<T, R> when;
    private final List<Predicate<R>> then = new ArrayList<>(3);

    public static <T, R> GWT<T, R> given(Runnable given) {
        GWT<T, R> instance = new GWT<>();
        instance.given = given;
        return instance;
    }

    public GWT<T, R> when(Function<T, R> when) {
        this.when = when;
        return this;
    }

    public GWT<T, R> withContext(T context) {
        this.context = context;
        return this;
    }

    public GWT<T, R> then(Predicate<R> assertion) {
        this.then.add(assertion);
        return this;
    }

    public boolean execute() {
        try {
            if (given != null) {
                given.run(); // Execute Given
            }
            R result = when.apply(context); // Execute When
            return then.stream().allMatch(assertion -> assertion.test(result)); // Execute Then
        } catch (Exception e) {
            return false;
        }
    }
}
