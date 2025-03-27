package org.app.common.pattern.revisited;

import java.util.function.Predicate;

public interface TypeSafeMarker<T> {
    boolean isValid(T instance);
    
    static <T> TypeSafeMarker<T> of(Predicate<T> validator) {
        return validator::test;
    }
    
    default TypeSafeMarker<T> and(TypeSafeMarker<T> other) {
        return instance -> this.isValid(instance) && other.isValid(instance);
    }
}