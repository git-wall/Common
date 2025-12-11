package org.app.common.db.jdbc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/// tận dụng được lambda expression của java để đảm bảo ít lỗi cũng như linh động hơn như jpa
/// mà vẫn đảm bảo tốc độ cao với (jdbc và jpa)
public class EntityMetadata {

    // Thread-safe maps
    private static final Map<Class<?>, Supplier<?>> NEW_INSTANCE_SUPPLIERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String> TABLE_NAMES = new ConcurrentHashMap<>();
    private static final Map<String, List<ColumnMapping<?>>> COLUMN_MAPPINGS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, RowMapper<?>> ROW_MAPPER_CACHE = new ConcurrentHashMap<>();

    public static <T> PreparedStatementCallback<T> getPreparedStatementCallback(int seconds) {
        return ps -> {
            ps.setQueryTimeout(seconds);
            return (T) ps;
        };
    }

    /**
     * Register constructor supplier for entity class
     */
    public static <T> void registerNewInstance(final Class<T> clazz, Supplier<T> supplier) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Objects.requireNonNull(supplier, "Supplier cannot be null");
        NEW_INSTANCE_SUPPLIERS.put(clazz, supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> getNewInstanceSupplier(final Class<T> clazz) {
        Supplier<T> supplier = (Supplier<T>) NEW_INSTANCE_SUPPLIERS.get(clazz);
        if (supplier == null) {
            throw new IllegalStateException("No instance supplier registered for " + clazz.getName());
        }
        return supplier;
    }

    /**
     * Register table name for entity class
     */
    public static void registerTableName(final Class<?> clazz, final String tableName) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Objects.requireNonNull(tableName, "Table name cannot be null");
        TABLE_NAMES.put(clazz, tableName);
    }

    public static String getTableName(final Class<?> clazz) {
        String tableName = TABLE_NAMES.get(clazz);
        if (tableName == null) {
            throw new IllegalStateException("No table name registered for " + clazz.getName());
        }
        return tableName;
    }

    /**
     * Register column mappings for entity class
     */
    public static <T> void registerColumnMappings(final Class<T> clazz, final List<ColumnMapping<T>> mappings) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Objects.requireNonNull(mappings, "Mappings cannot be null");

        String tableName = getTableName(clazz);
        @SuppressWarnings("unchecked")
        List<ColumnMapping<?>> rawMappings = (List<ColumnMapping<?>>) (List<?>) mappings;
        COLUMN_MAPPINGS.put(tableName, rawMappings);

        // Invalidate cache
        ROW_MAPPER_CACHE.remove(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<ColumnMapping<T>> getColumnMappings(final Class<T> clazz) {
        String tableName = getTableName(clazz);
        List<ColumnMapping<?>> mappings = COLUMN_MAPPINGS.get(tableName);
        if (mappings == null) {
            throw new IllegalStateException("No column mappings registered for " + clazz.getName());
        }
        return (List<ColumnMapping<T>>) (List<?>) mappings;
    }

    /**
     * Convert entity to parameter map for SQL
     */
    public static <T> Map<String, Object> toParamMap(final Class<T> clazz, T entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");

        List<ColumnMapping<T>> mappings = getColumnMappings(clazz);
        Map<String, Object> params = new HashMap<>(mappings.size());

        for (ColumnMapping<T> mapping : mappings) {
            params.put(mapping.getColumnName(), mapping.getValue(entity));
        }

        return params;
    }

    /**
     * Get cached RowMapper for entity class
     */
    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> getRowMapper(final Class<T> clazz) {
        return (RowMapper<T>) ROW_MAPPER_CACHE.computeIfAbsent(clazz,
            k -> (RowMapper<T>) (rs, rowNum) -> fromResultSet(rs, clazz));
    }
    /**
     * Map ResultSet to entity instance
     */
    public static <T> T fromResultSet(ResultSet rs, Class<T> clazz) {
        try {
            List<ColumnMapping<T>> mappings = getColumnMappings(clazz);
            Supplier<T> creator = getNewInstanceSupplier(clazz);

            T instance = creator.get();

            for (ColumnMapping<T> mapping : mappings) {
                try {
                    Object value = rs.getObject(mapping.getColumnName());
                    mapping.setValue(instance, value);
                } catch (SQLException e) {
                    // Log warning but continue - column might not exist in result set
                    System.err.println("Warning: Failed to map column " + mapping.getColumnName() +
                        " for class " + clazz.getName() + ": " + e.getMessage());
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map ResultSet to " + clazz.getName(), e);
        }
    }

    /**
     * Clear all registered metadata (useful for testing)
     */
    public static void clearAll() {
        NEW_INSTANCE_SUPPLIERS.clear();
        TABLE_NAMES.clear();
        COLUMN_MAPPINGS.clear();
        ROW_MAPPER_CACHE.clear();
    }

    /**
     * Type-safe column mapping
     */
    @Getter
    @AllArgsConstructor
    public static class ColumnMapping<T> {
        private final String columnName;
        private final Function<T, Object> getter;
        private final BiConsumer<T, Object> setter;

        public static <T> ColumnMapping<T> of(String columnName,
                                              Function<T, Object> getter,
                                              BiConsumer<T, Object> setter) {
            return new ColumnMapping<>(columnName, getter, setter);
        }

        public void setValue(T entity, Object value) {
            if (value != null) {
                setter.accept(entity, value);
            }
        }

        public Object getValue(T entity) {
            return getter.apply(entity);
        }
    }
}
