package org.app.core.accessor;

public interface PropertyAccessor {
    Object get(Object target);
    void set(Object target, Object value);
}
