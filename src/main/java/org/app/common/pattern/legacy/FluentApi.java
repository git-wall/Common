package org.app.common.pattern.legacy;

public abstract class FluentApi<T> {

    protected T self;

    public FluentApi() {
    }

    protected FluentApi(T self) {
        this.self = self;
    }

    @SuppressWarnings("unchecked")
    public T instance() {
        return (T) this;
    }
}
