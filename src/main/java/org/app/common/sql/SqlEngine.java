package org.app.common.sql;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Main entry point for creating and executing SQL queries.
 * This class provides factory methods for creating different types of SQL queries
 * and manages their execution.
 */
public class SqlEngine {
    
    @Getter
    @Setter
    private Function<String, Object> executionFunction;
    
    @Getter
    private final List<SqlQuery> executedQueries = new ArrayList<>();
    
    @Getter
    private final Map<String, Object> globalParameters = new HashMap<>();
    
    /**
     * Constructor with a custom execution function.
     * @param executionFunction the function to execute SQL queries
     */
    public SqlEngine(Function<String, Object> executionFunction) {
        this.executionFunction = executionFunction;
    }
    
    /**
     * Default constructor.
     * Uses a default execution function that just returns the SQL string.
     */
    public SqlEngine() {
        this(sql -> sql);
    }
    
    /**
     * Create a new SELECT query.
     * @return a new SELECT query
     */
    public SelectQuery select() {
        return new SelectQuery() {
            @Override
            public Object execute() {
                String sql = toSql();
                Object result = executionFunction.apply(sql);
                executedQueries.add(this);
                return result;
            }
        };
    }
    
    /**
     * Create a new INSERT query.
     * @return a new INSERT query
     */
    public InsertQuery insert() {
        return new InsertQuery() {
            @Override
            public Object execute() {
                String sql = toSql();
                Object result = executionFunction.apply(sql);
                executedQueries.add(this);
                return result;
            }
        };
    }
    
    /**
     * Create a new UPDATE query.
     * @return a new UPDATE query
     */
    public UpdateQuery update() {
        return new UpdateQuery() {
            @Override
            public Object execute() {
                String sql = toSql();
                Object result = executionFunction.apply(sql);
                executedQueries.add(this);
                return result;
            }
        };
    }
    
    /**
     * Create a new DELETE query.
     * @return a new DELETE query
     */
    public DeleteQuery delete() {
        return new DeleteQuery() {
            @Override
            public Object execute() {
                String sql = toSql();
                Object result = executionFunction.apply(sql);
                executedQueries.add(this);
                return result;
            }
        };
    }
    
    /**
     * Add a global parameter that will be available to all queries.
     * @param name the parameter name
     * @param value the parameter value
     * @return this engine for chaining
     */
    public SqlEngine addGlobalParameter(String name, Object value) {
        globalParameters.put(name, value);
        return this;
    }
    
    /**
     * Clear all global parameters.
     * @return this engine for chaining
     */
    public SqlEngine clearGlobalParameters() {
        globalParameters.clear();
        return this;
    }
    
    /**
     * Clear the list of executed queries.
     * @return this engine for chaining
     */
    public SqlEngine clearExecutedQueries() {
        executedQueries.clear();
        return this;
    }
    
    /**
     * Execute a raw SQL query.
     * @param sql the SQL query string
     * @return the result of the query execution
     */
    public Object executeRawSql(String sql) {
        return executionFunction.apply(sql);
    }
}