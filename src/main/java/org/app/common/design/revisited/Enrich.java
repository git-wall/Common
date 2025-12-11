package org.app.common.design.revisited;

import java.util.function.Consumer;

public class Enrich<T> {
    private final T t;

    public Enrich(T t) {
        this.t = t;
    }

    public static <T> Enrich<T> of(T t) {
        return new Enrich<>(t);
    }

    public Enrich<T> enrich(Consumer<T> enricher) {
        enricher.accept(t);
        return this;
    }
}
