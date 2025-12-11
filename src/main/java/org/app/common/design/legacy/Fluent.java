package org.app.common.design.legacy;

/**
 * This uses Fluent pattern and Curiously Recurring Template Pattern
 * */
public interface Fluent<T> {

    @SuppressWarnings("unchecked")
    default T self() {
        return (T) this;
    }

     default T chain(Runnable setter) {
        setter.run();
        return self();
    }
}
