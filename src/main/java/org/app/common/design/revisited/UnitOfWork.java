package org.app.common.design.revisited;

import java.util.function.Function;
import java.util.function.Supplier;

public class UnitOfWork<T> {
    private final Supplier<T> beginWork;
    private final Function<T, Boolean> commitWork;
    private final Function<T, Boolean> rollbackWork;

    public UnitOfWork(
            Supplier<T> beginWork,
            Function<T, Boolean> commitWork,
            Function<T, Boolean> rollbackWork) {
        this.beginWork = beginWork;
        this.commitWork = commitWork;
        this.rollbackWork = rollbackWork;
    }

    public <R> R execute(Function<T, R> work) {
        T context = beginWork.get();
        try {
            R result = work.apply(context);
            if (commitWork.apply(context)) {
                return result;
            }
            throw new RuntimeException("Failed to commit work");
        } catch (Exception e) {
            rollbackWork.apply(context);
            throw e;
        }
    }
}