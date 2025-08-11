package org.app.common.sql;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for SQL queries.
 * Provides common functionality for all SQL query types.
 */
@Getter
public abstract class AbstractSqlQuery implements SqlQuery {
    
    protected final SqlOperation operation;
    protected final List<SqlCriteria> criteria = new ArrayList<>();
    protected final Map<String, Object> parameters = new HashMap<>();
    
    /**
     * Constructor with the SQL operation type.
     * @param operation the SQL operation type
     */
    protected AbstractSqlQuery(SqlOperation operation) {
        this.operation = operation;
    }
    
    /**
     * Add a criterion to the query.
     * @param criterion the criterion to add
     * @return this query for chaining
     */
    public AbstractSqlQuery addCriterion(SqlCriteria criterion) {
        criteria.add(criterion);
        return this;
    }
    
    /**
     * Add a parameter to the query.
     * @param name the parameter name
     * @param value the parameter value
     * @return this query for chaining
     */
    public AbstractSqlQuery addParameter(String name, Object value) {
        parameters.put(name, value);
        return this;
    }
    
    /**
     * Get the SQL operation type of this query.
     * @return the SQL operation type
     */
    @Override
    public SqlOperation getOperation() {
        return operation;
    }
    
    /**
     * Get the parameters for the SQL query.
     * @return a map of parameter names to values
     */
    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    /**
     * Execute the SQL query and return the result.
     * This method should be implemented by concrete subclasses.
     * @return the result of the query execution
     */
    @Override
    public abstract Object execute();
    
    /**
     * Get the SQL query as a string.
     * This method should be implemented by concrete subclasses.
     * @return the SQL query string
     */
    @Override
    public abstract String toSql();
}