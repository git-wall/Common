package org.app.common.pattern.legacy;

/**
 * This use Fluent pattern + Curiously Recurring Template Pattern
 * */
public abstract class FluentApi<T extends FluentApi<T>> {

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
}
