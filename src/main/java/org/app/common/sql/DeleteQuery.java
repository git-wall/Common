package org.app.common.sql;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a SQL DELETE query.
 */
@Getter
public class DeleteQuery extends AbstractSqlQuery {
    
    private String table;
    private final List<SqlCriteria> whereConditions = new ArrayList<>();
    
    /**
     * Constructor for a DELETE query.
     */
    public DeleteQuery() {
        super(SqlOperation.DELETE);
    }
    
    /**
     * Set the table to delete from.
     * @param table the table name
     * @return this query for chaining
     */
    public DeleteQuery from(String table) {
        this.table = table;
        return this;
    }
    
    /**
     * Add a WHERE condition.
     * @param key the field name
     * @param operation the operation
     * @param value the value
     * @return this query for chaining
     */
    public DeleteQuery where(String key, SqlOperation operation, Object value) {
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
            throw new IllegalStateException("Table name must be specified for DELETE query");
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(table);
        
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