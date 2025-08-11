package org.app.common.sql;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents a SQL INSERT query.
 */
@Getter
public class InsertQuery extends AbstractSqlQuery {
    
    private String table;
    private final Map<String, Object> values = new LinkedHashMap<>();
    
    /**
     * Constructor for an INSERT query.
     */
    public InsertQuery() {
        super(SqlOperation.INSERT);
    }
    
    /**
     * Set the table to insert into.
     * @param table the table name
     * @return this query for chaining
     */
    public InsertQuery into(String table) {
        this.table = table;
        return this;
    }
    
    /**
     * Add a column-value pair to insert.
     * @param column the column name
     * @param value the value to insert
     * @return this query for chaining
     */
    public InsertQuery value(String column, Object value) {
        values.put(column, value);
        return this;
    }
    
    /**
     * Add multiple column-value pairs to insert.
     * @param values the column-value pairs
     * @return this query for chaining
     */
    public InsertQuery values(Map<String, Object> values) {
        this.values.putAll(values);
        return this;
    }
    
    /**
     * Build the SQL query string.
     * @return the SQL query string
     */
    @Override
    public String toSql() {
        if (table == null || table.isEmpty()) {
            throw new IllegalStateException("Table name must be specified for INSERT query");
        }
        
        if (values.isEmpty()) {
            throw new IllegalStateException("No values specified for INSERT query");
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(table).append(" (");
        
        StringJoiner columnJoiner = new StringJoiner(", ");
        for (String column : values.keySet()) {
            columnJoiner.add(column);
        }
        sql.append(columnJoiner.toString());
        
        sql.append(") VALUES (");
        
        StringJoiner valueJoiner = new StringJoiner(", ");
        for (Object value : values.values()) {
            if (value == null) {
                valueJoiner.add("NULL");
            } else if (value instanceof String) {
                valueJoiner.add("'" + value + "'");
            } else {
                valueJoiner.add(String.valueOf(value));
            }
        }
        sql.append(valueJoiner.toString());
        
        sql.append(")");
        
        return sql.toString();
    }
    
    /**
     * Execute the SQL query and return the result.
     * In a real implementation, this would execute the query against a database.
     * @return the result of the query execution
     */
    @Override
    public Object execute() {
        // In a real implementation, this would execute the query against a database
        // For now, we just return the SQL string
        return toSql();
    }
}