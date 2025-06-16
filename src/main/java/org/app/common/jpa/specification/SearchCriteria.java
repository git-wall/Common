package org.app.common.jpa.specification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

@Getter
@AllArgsConstructor
@Builder
public class SearchCriteria {
    private String key;
    private SearchOperation operation;
    private Object value;
    
    // For join operations
    private String joinAttribute;
    private JoinType joinType;
    private boolean isJoin;
    
    // For subquery operations
    private Class<?> subqueryClass;
    private String subqueryAttribute;
    private String mainEntityAttribute;
    private boolean isSubquery;

    public SearchCriteria(String key, SearchOperation operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
        this.isJoin = false;
        this.isSubquery = false;
    }
    
    public enum JoinType {
        INNER,
        LEFT,
        RIGHT
    }
}