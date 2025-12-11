package org.app.common.integration.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

// Hybrid Structure: GWT Wraps AAA
public class BDD<T,R> {
    private Runnable given;
    private Function<T, R> when;
    private final List<Predicate<R>> then = new ArrayList<>(3);
    private T context;

    public static <T, R> BDD<T, R> given(Runnable given) {
        BDD<T, R> instance = new BDD<>();
        instance.given = given;
        return instance;
    }

    public BDD<T, R> withContext(T context) {
        this.context = context;
        return this;
    }

    public BDD<T, R> when(Function<T, R> when) {
        this.when = when;
        return this;
    }

    public BDD<T, R> then(Predicate<R> assertion) {
        this.then.add(assertion);
        return this;
    }

    public boolean execute() {
        try {
            // 🔹 Given → Arrange
            if (given != null) {
                given.run();
            }

            // 🔹 When → Act
            R result = when.apply(context);

            // 🔹 Then → Assert
            return then.stream().allMatch(assertion -> assertion.test(result));

        } catch (Exception e) {
            return false;
        }
    }
}
