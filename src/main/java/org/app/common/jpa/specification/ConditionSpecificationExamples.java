package org.app.common.jpa.specification;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Examples of using ConditionSpecification for various query scenarios
 */
public class ConditionSpecificationExamples {

    /**
     * Example of basic equality conditions
     */
    public static <T> ConditionSpecification<T> equalityExample(String name, Long id) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("name")
                        .operation(SearchOperation.EQUALS)
                        .value(name)
                        .build())
                .add(SearchCriteria.builder()
                        .key("id")
                        .operation(SearchOperation.EQUALS)
                        .value(id)
                        .build());
    }

    /**
     * Example of inequality conditions
     */
    public static <T> ConditionSpecification<T> inequalityExample(String name, Long id) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("name")
                        .operation(SearchOperation.NOT_EQUALS)
                        .value(name)
                        .build())
                .add(SearchCriteria.builder()
                        .key("id")
                        .operation(SearchOperation.NOT_EQUALS)
                        .value(id)
                        .build());
    }

    /**
     * Example of string matching conditions
     */
    public static <T> ConditionSpecification<T> stringMatchingExample(String namePattern) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("name")
                        .operation(SearchOperation.LIKE)
                        .value(namePattern)
                        .build())
                .add(SearchCriteria.builder()
                        .key("code")
                        .operation(SearchOperation.STARTS_WITH)
                        .value("ABC")
                        .build())
                .add(SearchCriteria.builder()
                        .key("description")
                        .operation(SearchOperation.ENDS_WITH)
                        .value("test")
                        .build());
    }

    /**
     * Example of comparison conditions
     */
    public static <T> ConditionSpecification<T> comparisonExample(Long minPrice, Long maxPrice) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("price")
                        .operation(SearchOperation.GREATER_THAN)
                        .value(minPrice)
                        .build())
                .add(SearchCriteria.builder()
                        .key("price")
                        .operation(SearchOperation.LESS_THAN)
                        .value(maxPrice)
                        .build())
                .add(SearchCriteria.builder()
                        .key("quantity")
                        .operation(SearchOperation.GREATER_THAN_EQUAL)
                        .value(10)
                        .build())
                .add(SearchCriteria.builder()
                        .key("quantity")
                        .operation(SearchOperation.LESS_THAN_EQUAL)
                        .value(100)
                        .build());
    }

    /**
     * Example of date comparison conditions
     */
    public static <T> ConditionSpecification<T> dateComparisonExample(LocalDateTime startDate, LocalDateTime endDate) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("createdDate")
                        .operation(SearchOperation.GREATER_THAN)
                        .value(startDate)
                        .build())
                .add(SearchCriteria.builder()
                        .key("updatedDate")
                        .operation(SearchOperation.LESS_THAN)
                        .value(endDate)
                        .build());
    }

    /**
     * Example of timestamp comparison conditions
     */
    public static <T> ConditionSpecification<T> timestampComparisonExample(Timestamp startTime, Timestamp endTime) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("createdTime")
                        .operation(SearchOperation.GREATER_THAN_EQUAL)
                        .value(startTime)
                        .build())
                .add(SearchCriteria.builder()
                        .key("updatedTime")
                        .operation(SearchOperation.LESS_THAN_EQUAL)
                        .value(endTime)
                        .build());
    }

    /**
     * Example of collection membership conditions
     */
    public static <T> ConditionSpecification<T> collectionExample(List<Long> includedIds, List<String> excludedNames) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("id")
                        .operation(SearchOperation.IN)
                        .value(includedIds)
                        .build())
                .add(SearchCriteria.builder()
                        .key("name")
                        .operation(SearchOperation.NOT_IN)
                        .value(excludedNames)
                        .build());
    }

    /**
     * Example of null checking conditions
     */
    public static <T> ConditionSpecification<T> nullCheckExample() {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("deletedDate")
                        .operation(SearchOperation.IS_NULL)
                        .build())
                .add(SearchCriteria.builder()
                        .key("description")
                        .operation(SearchOperation.IS_NOT_NULL)
                        .build());
    }

    /**
     * Example of range conditions
     */
    public static <T> ConditionSpecification<T> rangeExample(Long minPrice, Long maxPrice,
                                                             LocalDateTime startDate, LocalDateTime endDate) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("price")
                        .operation(SearchOperation.BETWEEN)
                        .value(new ConditionSpecification.Range<>(minPrice, maxPrice))
                        .build())
                .add(SearchCriteria.builder()
                        .key("createdDate")
                        .operation(SearchOperation.BETWEEN)
                        .value(new ConditionSpecification.Range<>(startDate, endDate))
                        .build());
    }

    /**
     * Example of join conditions
     */
    public static <T> ConditionSpecification<T> joinExample(String categoryName, Long minOrderCount) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("name")
                        .operation(SearchOperation.EQUALS)
                        .value(categoryName)
                        .joinAttribute("category")
                        .joinType(SearchCriteria.JoinType.INNER)
                        .build())
                .add(SearchCriteria.builder()
                        .key("count")
                        .operation(SearchOperation.GREATER_THAN)
                        .value(minOrderCount)
                        .joinAttribute("orders")
                        .joinType(SearchCriteria.JoinType.LEFT)
                        .build());
    }

    /**
     * Example of subquery conditions
     */
    public static <T> ConditionSpecification<T> subqueryExample(Long minOrderValue, LocalDateTime orderDate) {
        return new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("value")
                        .operation(SearchOperation.GREATER_THAN)
                        .value(minOrderValue)
                        .subqueryClass(Order.class)
                        .subqueryAttribute("id")
                        .mainEntityAttribute("orderId")
                        .build())
                .add(SearchCriteria.builder()
                        .key("orderDate")
                        .operation(SearchOperation.LESS_THAN)
                        .value(orderDate)
                        .subqueryClass(Order.class)
                        .subqueryAttribute("id")
                        .mainEntityAttribute("orderId")
                        .build());
    }

    /**
     * Example of conditional criteria
     */
    public static <T> ConditionSpecification<T> conditionalExample(String name, Long id, Boolean includeInactive) {
        ConditionSpecification<T> spec = new ConditionSpecification<T>();

        // Add criteria only if name is not empty
        spec.whenNotEmpty(name, value -> SearchCriteria.builder()
                .key("name")
                .operation(SearchOperation.LIKE)
                .value(value)
                .build());

        // Add criteria only if id is not null
        spec.whenNotNull(id, value -> SearchCriteria.builder()
                .key("id")
                .operation(SearchOperation.EQUALS)
                .value(value)
                .build());

        // Add criteria based on a boolean condition
        spec.when(!Boolean.TRUE.equals(includeInactive), () -> SearchCriteria.builder()
                .key("active")
                .operation(SearchOperation.EQUALS)
                .value(true)
                .build());

        return spec;
    }

    /**
     * Example of combining specifications with AND
     */
    public static <T> ConditionSpecification<T> andExample(String name, Long minPrice) {
        ConditionSpecification<T> nameSpec = new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("name")
                        .operation(SearchOperation.LIKE)
                        .value(name)
                        .build());

        ConditionSpecification<T> priceSpec = new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("price")
                        .operation(SearchOperation.GREATER_THAN)
                        .value(minPrice)
                        .build());

        // Using static method
        return ConditionSpecification.and(nameSpec, priceSpec);

        // Alternatively using instance method
        // return nameSpec.and(priceSpec);
    }

    /**
     * Example of combining specifications with OR
     */
    public static <T> ConditionSpecification<T> orExample(String name, String code) {
        ConditionSpecification<T> nameSpec = new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("name")
                        .operation(SearchOperation.LIKE)
                        .value(name)
                        .build());

        ConditionSpecification<T> codeSpec = new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("code")
                        .operation(SearchOperation.EQUALS)
                        .value(code)
                        .build());

        // Using static method
        return ConditionSpecification.or(nameSpec, codeSpec);

        // Alternatively using instance method
        // return nameSpec.or(codeSpec);
    }

    /**
     * Example of negating a specification
     */
    public static <T> ConditionSpecification<T> notExample(String name) {
        ConditionSpecification<T> nameSpec = new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("name")
                        .operation(SearchOperation.EQUALS)
                        .value(name)
                        .build());

        return ConditionSpecification.not(nameSpec);
    }

    /**
     * Example of complex query combining multiple conditions
     */
    public static <T> ConditionSpecification<T> complexExample(String name, Long minPrice,
                                                               List<Long> categoryIds,
                                                               LocalDateTime startDate,
                                                               LocalDateTime endDate) {
        // Active products with matching name
        ConditionSpecification<T> nameSpec = new ConditionSpecification<T>()
                .add(SearchCriteria.builder()
                        .key("active")
                        .operation(SearchOperation.EQUALS)
                        .value(true)
                        .build())
                .whenNotEmpty(name, value -> SearchCriteria.builder()
                        .key("name")
                        .operation(SearchOperation.LIKE)
                        .value(value)
                        .build());

        // Products in specific categories with price above minimum
        ConditionSpecification<T> categoryAndPriceSpec = new ConditionSpecification<T>()
                .whenNotNull(categoryIds, value -> SearchCriteria.builder()
                        .key("categoryId")
                        .operation(SearchOperation.IN)
                        .value(value)
                        .build())
                .whenNotNull(minPrice, value -> SearchCriteria.builder()
                        .key("price")
                        .operation(SearchOperation.GREATER_THAN_EQUAL)
                        .value(value)
                        .build());

        // Products created within date range
        ConditionSpecification<T> dateRangeSpec = new ConditionSpecification<T>()
                .whenNotNull(startDate, value -> SearchCriteria.builder()
                        .key("createdDate")
                        .operation(SearchOperation.GREATER_THAN_EQUAL)
                        .value(value)
                        .build())
                .whenNotNull(endDate, value -> SearchCriteria.builder()
                        .key("createdDate")
                        .operation(SearchOperation.LESS_THAN_EQUAL)
                        .value(value)
                        .build());

        // Combine all specifications with AND
        return ConditionSpecification.and(nameSpec, categoryAndPriceSpec, dateRangeSpec);
    }

    /**
     * Example of using always true/false specifications
     */
    public static <T> ConditionSpecification<T> constantExample(boolean includeAll) {
        if (includeAll) {
            return ConditionSpecification.alwaysTrue();
        } else {
            return ConditionSpecification.alwaysFalse();
        }
    }

    // Dummy class for subquery example
    private static class Order {
        private Long id;
        private Long value;
        private LocalDateTime orderDate;
    }
}