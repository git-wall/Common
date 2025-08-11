package org.app.common.sql;

/**
 * Enum representing the different SQL operations that can be performed.
 */
public enum SqlOperation {
    // Query operations
    SELECT,
    
    // Data manipulation operations
    INSERT,
    UPDATE,
    DELETE,
    
    // Clause operations
    FROM,
    WHERE,
    GROUP_BY,
    HAVING,
    ORDER_BY,
    LIMIT,
    OFFSET,
    
    // Join operations
    JOIN,
    LEFT_JOIN,
    RIGHT_JOIN,
    INNER_JOIN,
    OUTER_JOIN,
    
    // Logical operations
    AND,
    OR,
    NOT,
    
    // Comparison operations
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_EQUAL,
    LESS_THAN_EQUAL,
    
    // String operations
    LIKE,
    NOT_LIKE,
    STARTS_WITH,
    ENDS_WITH,
    
    // Collection operations
    IN,
    NOT_IN,
    
    // Null operations
    IS_NULL,
    IS_NOT_NULL,
    
    // Range operations
    BETWEEN,
    
    // Aggregate functions
    AVG,
    MIN,
    MAX,
    COUNT,
    SUM,
    
    // Other operations
    AS,
    DISTINCT,
    EXISTS,
    NOT_EXISTS,
    UNION,
    UNION_ALL,
    INTERSECT,
    EXCEPT
}