package org.app.common.pattern.revisited;

import java.util.function.Consumer;
import java.util.function.Function;

public class ExecuteAround<T, R> {
    private final Function<T, R> resourceAcquisition;
    private final Consumer<R> resourceRelease;

    public ExecuteAround(Function<T, R> resourceAcquisition, Consumer<R> resourceRelease) {
        this.resourceAcquisition = resourceAcquisition;
        this.resourceRelease = resourceRelease;
    }

    public void execute(T context, Consumer<R> operation) {
        R resource = resourceAcquisition.apply(context);
        try {
            operation.accept(resource);
        } finally {
            resourceRelease.accept(resource);
        }
    }

    public <V> V executeWithResult(T context, Function<R, V> operation) {
        R resource = resourceAcquisition.apply(context);
        try {
            return operation.apply(resource);
        } finally {
            resourceRelease.accept(resource);
        }
    }
}