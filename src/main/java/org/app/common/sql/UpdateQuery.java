package org.app.common.sql;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a SQL UPDATE query.
 */
@Getter
public class UpdateQuery extends AbstractSqlQuery {
    
    private String table;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private final List<SqlCriteria> whereConditions = new ArrayList<>();
    
    /**
     * Constructor for an UPDATE query.
     */
    public UpdateQuery() {
        super(SqlOperation.UPDATE);
    }
    
    /**
     * Set the table to update.
     * @param table the table name
     * @return this query for chaining
     */
    public UpdateQuery table(String table) {
        this.table = table;
        return this;
    }
    
    /**
     * Add a column-value pair to update.
     * @param column the column name
     * @param value the new value
     * @return this query for chaining
     */
    public UpdateQuery set(String column, Object value) {
        values.put(column, value);
        return this;
    }
    
    /**
     * Add multiple column-value pairs to update.
     * @param values the column-value pairs
     * @return this query for chaining
     */
    public UpdateQuery setAll(Map<String, Object> values) {
        this.values.putAll(values);
        return this;
    }
    
    /**
     * Add a WHERE condition.
     * @param key the field name
     * @param operation the operation
     * @param value the value
     * @return this query for chaining
     */
    public UpdateQuery where(String key, SqlOperation operation, Object value) {
        whereConditions.add(new SqlCriteria(key, operation, value));
        return this;
    }
    
    /**
     * Build the SQL query string.
     * @return the SQL query string
     */
    @Override
    public String toSql() {
        if (table == null || table.isEmpty()) {
            throw new IllegalStateException("Table name must be specified for UPDATE query");
        }
        
        if (values.isEmpty()) {
            throw new IllegalStateException("No values specified for UPDATE query");
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(table).append(" SET ");
        
        sql.append(values.entrySet().stream()
                .map(entry -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(entry.getKey()).append(" = ");
                    Object value = entry.getValue();
                    if (value == null) {
                        sb.append("NULL");
                    } else if (value instanceof String) {
                        sb.append("'").append(value).append("'");
                    } else {
                        sb.append(value);
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining(", ")));
        
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(buildConditionsString(whereConditions));
        }
        
        return sql.toString();
    }
    
    /**
     * Build a string representation of a list of conditions.
     * @param conditions the conditions to build a string for
     * @return the string representation
     */
    private String buildConditionsString(List<SqlCriteria> conditions) {
        return conditions.stream()
                .map(this::buildConditionString)
                .collect(Collectors.joining(" AND "));
    }
    
    /**
     * Build a string representation of a condition.
     * @param condition the condition to build a string for
     * @return the string representation
     */
    private String buildConditionString(SqlCriteria condition) {
        StringBuilder sb = new StringBuilder();
        
        String key = condition.getKey();
        SqlOperation operation = condition.getOperation();
        Object value = condition.getValue();
        
        switch (operation) {
            case EQUALS:
                sb.append(key).append(" = ");
                appendValue(sb, value);
                break;
            case NOT_EQUALS:
                sb.append(key).append(" != ");
                appendValue(sb, value);
                break;
            case GREATER_THAN:
                sb.append(key).append(" > ");
                appendValue(sb, value);
                break;
            case LESS_THAN:
                sb.append(key).append(" < ");
                appendValue(sb, value);
                break;
            case GREATER_THAN_EQUAL:
                sb.append(key).append(" >= ");
                appendValue(sb, value);
                break;
            case LESS_THAN_EQUAL:
                sb.append(key).append(" <= ");
                appendValue(sb, value);
                break;
            case LIKE:
                sb.append(key).append(" LIKE ");
                appendValue(sb, value);
                break;
            case IN:
                sb.append(key).append(" IN (");
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    sb.append(list.stream()
                            .map(item -> item instanceof String ? "'" + item + "'" : String.valueOf(item))
                            .collect(Collectors.joining(", ")));
                }
                sb.append(")");
                break;
            case IS_NULL:
                sb.append(key).append(" IS NULL");
                break;
            case IS_NOT_NULL:
                sb.append(key).append(" IS NOT NULL");
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
        
        return sb.toString();
    }
    
    /**
     * Append a value to a string builder, handling different types.
     * @param sb the string builder
     * @param value the value to append
     */
    private void appendValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("NULL");
        } else if (value instanceof String) {
            sb.append("'").append(value).append("'");
        } else {
            sb.append(value);
        }
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