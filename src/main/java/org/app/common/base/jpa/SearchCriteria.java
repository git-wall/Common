package org.app.common.base.jpa;

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
    
    public static SearchCriteria createJoinCriteria(String joinAttribute, JoinType joinType, 
                                                   String key, SearchOperation operation, Object value) {
        SearchCriteria criteria = new SearchCriteria(key, operation, value);
        criteria.joinAttribute = joinAttribute;
        criteria.joinType = joinType;
        criteria.isJoin = true;
        return criteria;
    }
    
    public static SearchCriteria createSubqueryCriteria(Class<?> subqueryClass, String subqueryAttribute, 
                                                       String mainEntityAttribute, SearchOperation operation, Object value) {
        SearchCriteria criteria = new SearchCriteria(subqueryAttribute, operation, value);
        criteria.subqueryClass = subqueryClass;
        criteria.mainEntityAttribute = mainEntityAttribute;
        criteria.isSubquery = true;
        return criteria;
    }
    
    public enum JoinType {
        INNER,
        LEFT,
        RIGHT
    }
}