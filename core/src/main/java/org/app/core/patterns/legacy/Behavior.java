package org.app.core.patterns.legacy;

public interface Behavior<T> {
    T execute(T input);
}
