package org.app.common.pattern.test;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PresentationModel<T> {
    @Getter
    private T model;
    private final List<Consumer<T>> observers = new ArrayList<>(16);

    public void setModel(T model) {
        this.model = model;
        notifyObservers();
    }

    public void addObserver(Consumer<T> observer) {
        observers.add(observer);
    }

    public void removeObserver(Consumer<T> observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        observers.forEach(observer -> observer.accept(model));
    }
}