package org.app.common.jpa.specification;

public enum SearchOperation {
    EQUALS,
    LIKE,
    GREATER_THAN,
    LESS_THAN,
    NOT_EQUALS,
    STARTS_WITH,
    ENDS_WITH,
    GREATER_THAN_EQUAL,
    LESS_THAN_EQUAL,
    IN,
    NOT_IN,
    IS_NULL,
    IS_NOT_NULL,
    BETWEEN,
    EXISTS,
    NOT_EXISTS
}