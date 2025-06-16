package org.app.common.base.entities;

public abstract class BaseModel<T> {
    private T id;

    public BaseModel(T id) {
        this.id = id;
    }

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }
}
