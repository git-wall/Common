package org.app.common.sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a SQL criterion with a key, operation, and value.
 * Used to build SQL queries dynamically.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlCriteria {
    private String key;
    private SqlOperation operation;
    private Object value;
    
    // For join operations
    private String joinTable;
    private JoinType joinType;
    private String joinCondition;
    
    // For aggregate functions
    private String alias;
    private boolean distinct;
    
    // For subqueries
    private SqlQuery subquery;
    
    /**
     * Simple constructor for basic criteria
     */
    public SqlCriteria(String key, SqlOperation operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }
    
    /**
     * Join types in SQL
     */
    public enum JoinType {
        INNER,
        LEFT,
        RIGHT,
        FULL
    }
    
    /**
     * Represents a range of values for BETWEEN operations
     */
    @Getter
    @AllArgsConstructor
    public static class Range<T> {
        private final T from;
        private final T to;
    }
}