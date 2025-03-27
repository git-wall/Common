package org.app.common.pattern.legacy;

public class Pipeline<I, O> {

    private Action<I, O> action;

    public Pipeline() {
    }

    public Pipeline(Action<I, O> action) {
        this.action = action;
    }

    public static <I, O> Pipeline<I, O> of() {
        return new Pipeline<>();
    }

    public <K> Pipeline<I, K> next(Action<O, K> newAction) {
        return new Pipeline<>(i -> newAction.process(action.process(i)));
    }

    public O process(I input) {
        return action.process(input);
    }

    public interface Action<I, O> {
        O process(I input);
    }
}
