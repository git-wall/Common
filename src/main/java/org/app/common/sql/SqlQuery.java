package org.app.common.sql;

import java.util.Map;

/**
 * Interface representing a SQL query.
 * This is the base interface for all SQL queries.
 */
public interface SqlQuery {
    
    /**
     * Get the SQL operation type of this query.
     * @return the SQL operation type
     */
    SqlOperation getOperation();
    
    /**
     * Get the SQL query as a string.
     * @return the SQL query string
     */
    String toSql();
    
    /**
     * Get the parameters for the SQL query.
     * @return a map of parameter names to values
     */
    Map<String, Object> getParameters();
    
    /**
     * Execute the SQL query and return the result.
     * @return the result of the query execution
     */
    Object execute();
}