package org.app.common.sql;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a SQL SELECT query.
 */
@Getter
public class SelectQuery extends AbstractSqlQuery {
    
    private final List<String> selectFields = new ArrayList<>();
    private final List<String> fromTables = new ArrayList<>();
    private final List<SqlCriteria> whereConditions = new ArrayList<>();
    private final List<SqlCriteria> joinConditions = new ArrayList<>();
    private final List<String> groupByFields = new ArrayList<>();
    private final List<SqlCriteria> havingConditions = new ArrayList<>();
    private final List<String> orderByFields = new ArrayList<>();
    private boolean distinct = false;
    private Integer limit;
    private Integer offset;
    
    /**
     * Constructor for a SELECT query.
     */
    public SelectQuery() {
        super(SqlOperation.SELECT);
    }
    
    /**
     * Add fields to select.
     * @param fields the fields to select
     * @return this query for chaining
     */
    public SelectQuery select(String... fields) {
        selectFields.addAll(Arrays.asList(fields));
        return this;
    }
    
    /**
     * Add a field with an aggregate function.
     * @param function the aggregate function
     * @param field the field to apply the function to
     * @param alias the alias for the result
     * @return this query for chaining
     */
    public SelectQuery selectFunction(SqlOperation function, String field, String alias) {
        if (isAggregateFunction(function)) {
            selectFields.add(function.name() + "(" + field + ") AS " + alias);
        }
        return this;
    }
    
    /**
     * Set the query to select distinct results.
     * @return this query for chaining
     */
    public SelectQuery distinct() {
        this.distinct = true;
        return this;
    }
    
    /**
     * Add tables to select from.
     * @param tables the tables to select from
     * @return this query for chaining
     */
    public SelectQuery from(String... tables) {
        fromTables.addAll(Arrays.asList(tables));
        return this;
    }
    
    /**
     * Add a WHERE condition.
     * @param key the field name
     * @param operation the operation
     * @param value the value
     * @return this query for chaining
     */
    public SelectQuery where(String key, SqlOperation operation, Object value) {
        whereConditions.add(new SqlCriteria(key, operation, value));
        return this;
    }
    
    /**
     * Add a JOIN condition.
     * @param table the table to join
     * @param joinType the type of join
     * @param condition the join condition
     * @return this query for chaining
     */
    public SelectQuery join(String table, SqlCriteria.JoinType joinType, String condition) {
        SqlCriteria joinCriteria = SqlCriteria.builder()
                .joinTable(table)
                .joinType(joinType)
                .joinCondition(condition)
                .build();
        joinConditions.add(joinCriteria);
        return this;
    }
    
    /**
     * Add GROUP BY fields.
     * @param fields the fields to group by
     * @return this query for chaining
     */
    public SelectQuery groupBy(String... fields) {
        groupByFields.addAll(Arrays.asList(fields));
        return this;
    }
    
    /**
     * Add a HAVING condition.
     * @param key the field name
     * @param operation the operation
     * @param value the value
     * @return this query for chaining
     */
    public SelectQuery having(String key, SqlOperation operation, Object value) {
        havingConditions.add(new SqlCriteria(key, operation, value));
        return this;
    }
    
    /**
     * Add ORDER BY fields.
     * @param fields the fields to order by
     * @return this query for chaining
     */
    public SelectQuery orderBy(String... fields) {
        Collections.addAll(orderByFields, fields);
        return this;
    }
    
    /**
     * Set the LIMIT clause.
     * @param limit the maximum number of rows to return
     * @return this query for chaining
     */
    public SelectQuery limit(int limit) {
        this.limit = limit;
        return this;
    }
    
    /**
     * Set the OFFSET clause.
     * @param offset the number of rows to skip
     * @return this query for chaining
     */
    public SelectQuery offset(int offset) {
        this.offset = offset;
        return this;
    }
    
    /**
     * Check if an operation is an aggregate function.
     * @param operation the operation to check
     * @return true if the operation is an aggregate function
     */
    private boolean isAggregateFunction(SqlOperation operation) {
        return operation == SqlOperation.AVG ||
               operation == SqlOperation.MIN ||
               operation == SqlOperation.MAX ||
               operation == SqlOperation.COUNT ||
               operation == SqlOperation.SUM;
    }
    
    /**
     * Build the SQL query string.
     * @return the SQL query string
     */
    @Override
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        
        // SELECT clause
        sql.append("SELECT ");
        if (distinct) {
            sql.append("DISTINCT ");
        }
        
        if (selectFields.isEmpty()) {
            sql.append("*");
        } else {
            sql.append(String.join(", ", selectFields));
        }
        
        // FROM clause
        if (!fromTables.isEmpty()) {
            sql.append(" FROM ").append(String.join(", ", fromTables));
        }
        
        // JOIN clauses
        for (SqlCriteria join : joinConditions) {
            sql.append(" ");
            switch (join.getJoinType()) {
                case INNER:
                    sql.append("INNER JOIN ");
                    break;
                case LEFT:
                    sql.append("LEFT JOIN ");
                    break;
                case RIGHT:
                    sql.append("RIGHT JOIN ");
                    break;
                case FULL:
                    sql.append("FULL OUTER JOIN ");
                    break;
            }
            sql.append(join.getJoinTable())
               .append(" ON ")
               .append(join.getJoinCondition());
        }
        
        // WHERE clause
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(buildConditionsString(whereConditions));
        }
        
        // GROUP BY clause
        if (!groupByFields.isEmpty()) {
            sql.append(" GROUP BY ").append(String.join(", ", groupByFields));
        }
        
        // HAVING clause
        if (!havingConditions.isEmpty()) {
            sql.append(" HAVING ");
            sql.append(buildConditionsString(havingConditions));
        }
        
        // ORDER BY clause
        if (!orderByFields.isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", orderByFields));
        }
        
        // LIMIT and OFFSET clauses
        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }
        
        if (offset != null) {
            sql.append(" OFFSET ").append(offset);
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
            case NOT_LIKE:
                sb.append(key).append(" NOT LIKE ");
                appendValue(sb, value);
                break;
            case STARTS_WITH:
                sb.append(key).append(" LIKE ");
                appendValue(sb, value + "%");
                break;
            case ENDS_WITH:
                sb.append(key).append(" LIKE ");
                appendValue(sb, "%" + value);
                break;
            case IN:
                sb.append(key).append(" IN (");
                appendInValues(sb, value);
                sb.append(")");
                break;
            case NOT_IN:
                sb.append(key).append(" NOT IN (");
                appendInValues(sb, value);
                sb.append(")");
                break;
            case IS_NULL:
                sb.append(key).append(" IS NULL");
                break;
            case IS_NOT_NULL:
                sb.append(key).append(" IS NOT NULL");
                break;
            case BETWEEN:
                if (value instanceof SqlCriteria.Range) {
                    SqlCriteria.Range<?> range = (SqlCriteria.Range<?>) value;
                    sb.append(key).append(" BETWEEN ");
                    appendValue(sb, range.getFrom());
                    sb.append(" AND ");
                    appendValue(sb, range.getTo());
                }
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
     * Append a list of values for IN clauses.
     * @param sb the string builder
     * @param value the list of values
     */
    private void appendInValues(StringBuilder sb, Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringJoiner joiner = new StringJoiner(", ");
            for (Object item : list) {
                if (item instanceof String) {
                    joiner.add("'" + item + "'");
                } else {
                    joiner.add(String.valueOf(item));
                }
            }
            sb.append(joiner.toString());
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