package org.app.common.pattern.revisited;

import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;

public class Memento<T> {
    private final Stack<T> states = new Stack<>();
    private final Function<T, T> cloner;

    public Memento(T initialState, Function<T, T> cloner) {
        this.cloner = cloner;
        saveState(initialState);
    }

    public void saveState(T state) {
        states.push(cloner.apply(state));
    }

    public Optional<T> undo() {
        if (states.size() > 1) {
            states.pop();
            return Optional.of(getCurrentState());
        }
        return Optional.empty();
    }

    public T getCurrentState() {
        return cloner.apply(states.peek());
    }
}