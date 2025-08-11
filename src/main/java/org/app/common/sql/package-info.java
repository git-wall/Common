/**
 * SQL Engine Package
 * 
 * <p>A flexible and type-safe SQL query builder for Java applications.</p>
 * 
 * <h2>Overview</h2>
 * 
 * <p>The SQL Engine provides a fluent API for building SQL queries in a type-safe way. 
 * It supports various SQL operations including SELECT, INSERT, UPDATE, and DELETE, 
 * as well as clauses like WHERE, JOIN, GROUP BY, HAVING, and more.</p>
 * 
 * <h2>Features</h2>
 * 
 * <ul>
 *   <li>Type-safe query building</li>
 *   <li>Support for SELECT, INSERT, UPDATE, and DELETE operations</li>
 *   <li>Support for WHERE, JOIN, GROUP BY, HAVING, ORDER BY, LIMIT, and OFFSET clauses</li>
 *   <li>Support for aggregate functions (AVG, MIN, MAX, COUNT, SUM)</li>
 *   <li>Support for various comparison operations (EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, etc.)</li>
 *   <li>Support for string operations (LIKE, STARTS_WITH, ENDS_WITH)</li>
 *   <li>Support for collection operations (IN, NOT_IN)</li>
 *   <li>Support for null operations (IS_NULL, IS_NOT_NULL)</li>
 *   <li>Support for range operations (BETWEEN)</li>
 *   <li>Customizable query execution</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * 
 * <p>See the {@link org.app.common.engine.sql.SqlEngineExample} class for examples of how to use the SQL engine.</p>
 * 
 * <h3>Main Classes</h3>
 * 
 * <ul>
 *   <li>{@link org.app.common.engine.sql.SqlEngine} - Main entry point for creating and executing SQL queries</li>
 *   <li>{@link org.app.common.engine.sql.SqlOperation} - Enum of SQL operations</li>
 *   <li>{@link org.app.common.engine.sql.SqlCriteria} - Represents a SQL criterion</li>
 *   <li>{@link org.app.common.engine.sql.SqlQuery} - Interface for SQL queries</li>
 *   <li>{@link org.app.common.engine.sql.AbstractSqlQuery} - Abstract base class for SQL queries</li>
 *   <li>{@link org.app.common.engine.sql.SelectQuery} - Represents a SQL SELECT query</li>
 *   <li>{@link org.app.common.engine.sql.InsertQuery} - Represents a SQL INSERT query</li>
 *   <li>{@link org.app.common.engine.sql.UpdateQuery} - Represents a SQL UPDATE query</li>
 *   <li>{@link org.app.common.engine.sql.DeleteQuery} - Represents a SQL DELETE query</li>
 * </ul>
 */
package org.app.common.sql;