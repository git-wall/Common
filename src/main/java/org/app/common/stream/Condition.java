package org.app.common.stream;

import java.util.Collection;
import java.util.Objects;

interface Condition {
    boolean test(Object value);

    static Condition EQUALS(Object compareValue) {
        return value -> Objects.equals(value, compareValue);
    }

    static Condition NOT_EQUALS(Object compareValue) {
        return value -> !Objects.equals(value, compareValue);
    }

    static Condition GREATER_THAN(Comparable compareValue) {
        return value -> value instanceof Comparable &&
                ((Comparable) value).compareTo(compareValue) > 0;
    }

    static Condition LESS_THAN(Comparable compareValue) {
        return value -> value instanceof Comparable &&
                ((Comparable) value).compareTo(compareValue) < 0;
    }

    static Condition GREATER_THAN_EQUAL(Comparable compareValue) {
        return value -> value instanceof Comparable &&
                ((Comparable) value).compareTo(compareValue) >= 0;
    }

    static Condition LESS_THAN_EQUAL(Comparable compareValue) {
        return value -> value instanceof Comparable &&
                ((Comparable) value).compareTo(compareValue) <= 0;
    }

    static Condition LIKE(String pattern) {
        return value -> value != null && value.toString().contains(pattern);
    }

    static Condition NOT_LIKE(String pattern) {
        return value -> value == null || !value.toString().contains(pattern);
    }

    static Condition STARTS_WITH(String prefix) {
        return value -> value != null && value.toString().startsWith(prefix);
    }

    static Condition ENDS_WITH(String suffix) {
        return value -> value != null && value.toString().endsWith(suffix);
    }

    static Condition IN(Collection<?> values) {
        return value -> values.contains(value);
    }

    static Condition NOT_IN(Collection<?> values) {
        return value -> !values.contains(value);
    }

    static Condition IS_NULL() {
        return value -> value == null;
    }

    static Condition IS_NOT_NULL() {
        return value -> value != null;
    }

    static Condition BETWEEN(Comparable min, Comparable max) {
        return value -> value instanceof Comparable &&
                ((Comparable) value).compareTo(min) >= 0 &&
                ((Comparable) value).compareTo(max) <= 0;
    }

    // Logical operations
    default Condition AND(Condition other) {
        return value -> this.test(value) && other.test(value);
    }

    default Condition OR(Condition other) {
        return value -> this.test(value) || other.test(value);
    }

    default Condition NOT() {
        return value -> !this.test(value);
    }
}
