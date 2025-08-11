package org.app.common.sql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Examples of using the SQL engine for various query scenarios.
 */
public class SqlEngineExample {

    /**
     * Main method to run the examples.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Create a SQL engine with a custom execution function
        SqlEngine sqlEngine = new SqlEngine(sql -> {
            System.out.println("Executing SQL: " + sql);
            return "Result of: " + sql;
        });
        
        // Run the examples
        System.out.println("\n--- SELECT Examples ---");
        runSelectExamples(sqlEngine);
        
        System.out.println("\n--- INSERT Examples ---");
        runInsertExamples(sqlEngine);
        
        System.out.println("\n--- UPDATE Examples ---");
        runUpdateExamples(sqlEngine);
        
        System.out.println("\n--- DELETE Examples ---");
        runDeleteExamples(sqlEngine);
        
        System.out.println("\n--- Complex Query Examples ---");
        runComplexQueryExamples(sqlEngine);
    }
    
    /**
     * Examples of SELECT queries.
     * @param sqlEngine the SQL engine
     */
    private static void runSelectExamples(SqlEngine sqlEngine) {
        // Simple SELECT
        Object result1 = sqlEngine.select()
                .from("users")
                .execute();
        System.out.println("Result 1: " + result1);
        
        // SELECT with WHERE
        Object result2 = sqlEngine.select()
                .select("id", "name", "email")
                .from("users")
                .where("active", SqlOperation.EQUALS, true)
                .execute();
        System.out.println("Result 2: " + result2);
        
        // SELECT with multiple conditions
        Object result3 = sqlEngine.select()
                .select("id", "name", "email")
                .from("users")
                .where("age", SqlOperation.GREATER_THAN, 18)
                .where("age", SqlOperation.LESS_THAN, 65)
                .execute();
        System.out.println("Result 3: " + result3);
        
        // SELECT with JOIN
        Object result4 = sqlEngine.select()
                .select("u.id", "u.name", "o.order_date")
                .from("users u")
                .join("orders o", SqlCriteria.JoinType.INNER, "u.id = o.user_id")
                .where("o.status", SqlOperation.EQUALS, "COMPLETED")
                .execute();
        System.out.println("Result 4: " + result4);
        
        // SELECT with GROUP BY and aggregate functions
        Object result5 = sqlEngine.select()
                .select("department")
                .selectFunction(SqlOperation.AVG, "salary", "avg_salary")
                .selectFunction(SqlOperation.COUNT, "id", "employee_count")
                .from("employees")
                .groupBy("department")
                .having("COUNT(id)", SqlOperation.GREATER_THAN, 5)
                .execute();
        System.out.println("Result 5: " + result5);
        
        // SELECT with ORDER BY, LIMIT, and OFFSET
        Object result6 = sqlEngine.select()
                .select("id", "name", "created_at")
                .from("products")
                .where("category", SqlOperation.EQUALS, "Electronics")
                .orderBy("created_at DESC")
                .limit(10)
                .offset(20)
                .execute();
        System.out.println("Result 6: " + result6);
        
        // SELECT with IN condition
        Object result7 = sqlEngine.select()
                .select("id", "name")
                .from("products")
                .where("category_id", SqlOperation.IN, Arrays.asList(1, 2, 3))
                .execute();
        System.out.println("Result 7: " + result7);
        
        // SELECT with LIKE condition
        Object result8 = sqlEngine.select()
                .select("id", "name")
                .from("products")
                .where("name", SqlOperation.LIKE, "%phone%")
                .execute();
        System.out.println("Result 8: " + result8);
    }
    
    /**
     * Examples of INSERT queries.
     * @param sqlEngine the SQL engine
     */
    private static void runInsertExamples(SqlEngine sqlEngine) {
        // Simple INSERT
        Object result1 = sqlEngine.insert()
                .into("users")
                .value("name", "John Doe")
                .value("email", "john.doe@example.com")
                .value("age", 30)
                .value("active", true)
                .execute();
        System.out.println("Result 1: " + result1);
        
        // INSERT with multiple values
        Map<String, Object> userValues = new HashMap<>();
        userValues.put("name", "Jane Smith");
        userValues.put("email", "jane.smith@example.com");
        userValues.put("age", 25);
        userValues.put("active", true);
        
        Object result2 = sqlEngine.insert()
                .into("users")
                .values(userValues)
                .execute();
        System.out.println("Result 2: " + result2);
    }
    
    /**
     * Examples of UPDATE queries.
     * @param sqlEngine the SQL engine
     */
    private static void runUpdateExamples(SqlEngine sqlEngine) {
        // Simple UPDATE
        Object result1 = sqlEngine.update()
                .table("users")
                .set("active", false)
                .where("last_login", SqlOperation.LESS_THAN, "2023-01-01")
                .execute();
        System.out.println("Result 1: " + result1);
        
        // UPDATE with multiple values
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("status", "INACTIVE");
        updateValues.put("updated_at", "2023-06-01");
        
        Object result2 = sqlEngine.update()
                .table("orders")
                .setAll(updateValues)
                .where("status", SqlOperation.EQUALS, "PENDING")
                .where("created_at", SqlOperation.LESS_THAN, "2023-01-01")
                .execute();
        System.out.println("Result 2: " + result2);
    }
    
    /**
     * Examples of DELETE queries.
     * @param sqlEngine the SQL engine
     */
    private static void runDeleteExamples(SqlEngine sqlEngine) {
        // Simple DELETE
        Object result1 = sqlEngine.delete()
                .from("users")
                .where("active", SqlOperation.EQUALS, false)
                .execute();
        System.out.println("Result 1: " + result1);
        
        // DELETE with multiple conditions
        Object result2 = sqlEngine.delete()
                .from("orders")
                .where("status", SqlOperation.EQUALS, "CANCELLED")
                .where("created_at", SqlOperation.LESS_THAN, "2023-01-01")
                .execute();
        System.out.println("Result 2: " + result2);
    }
    
    /**
     * Examples of complex queries.
     * @param sqlEngine the SQL engine
     */
    private static void runComplexQueryExamples(SqlEngine sqlEngine) {
        // Complex SELECT with multiple JOINs, GROUP BY, and HAVING
        Object result1 = sqlEngine.select()
                .select("c.name AS category_name")
                .selectFunction(SqlOperation.COUNT, "p.id", "product_count")
                .selectFunction(SqlOperation.AVG, "p.price", "avg_price")
                .from("products p")
                .join("categories c", SqlCriteria.JoinType.INNER, "p.category_id = c.id")
                .join("product_tags pt", SqlCriteria.JoinType.LEFT, "p.id = pt.product_id")
                .join("tags t", SqlCriteria.JoinType.LEFT, "pt.tag_id = t.id")
                .where("p.active", SqlOperation.EQUALS, true)
                .where("t.name", SqlOperation.IN, Arrays.asList("sale", "new", "featured"))
                .groupBy("c.name")
                .having("COUNT(p.id)", SqlOperation.GREATER_THAN, 5)
                .orderBy("product_count DESC")
                .limit(10)
                .execute();
        System.out.println("Result 1: " + result1);
        
        // Complex UPDATE with subquery (simulated)
        System.out.println("Complex UPDATE with subquery (simulated):");
        System.out.println("UPDATE products SET discount = 0.1 WHERE category_id IN (SELECT id FROM categories WHERE name LIKE '%electronics%')");
        
        // Complex DELETE with subquery (simulated)
        System.out.println("Complex DELETE with subquery (simulated):");
        System.out.println("DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE status = 'CANCELLED')");
    }
}