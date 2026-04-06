package org.app.common.db;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.app.common.utils.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class BaseRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    @Getter
    private final JdbcTemplate simpleJdbcTemplate;

    // === CRUD Operations ===

    public int insert(String sql, Map<String, ?> params) {
        return jdbcTemplate.update(sql, params);
    }

    public Number insertReturningId(String sql, Map<String, ?> params) {
        Number id = jdbcTemplate.queryForObject(sql, params, Number.class);
        if (id == null) {
            throw new IllegalStateException("Insert did not return an ID");
        }
        return id;
    }

    public int update(String sql, Map<String, ?> params) {
        return jdbcTemplate.update(sql, params);
    }

    public int delete(String sql, Map<String, ?> params) {
        return jdbcTemplate.update(sql, params);
    }

    // === Query Operations ===

    public <T> List<T> findAnyMatch(String sql, Map<String, ?> params, Class<T> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        return jdbcTemplate.query(sql, params, RowMapperFactory.fromClass(clazz));
    }

    public <T> Optional<T> findFirst(String sql, Map<String, ?> params, Class<T> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");

        List<T> results = jdbcTemplate.query(sql, params, RowMapperFactory.fromClass(clazz));

        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new IllegalStateException("Query returned more than one result");
        }
        return Optional.of(results.get(0));
    }

    public boolean exists(String sql, Map<String, ?> params) {
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    public <T> T findFirst(String sql, PreparedStatementCallback<T> action) {
        return simpleJdbcTemplate.execute(sql, action);
    }

    // === Batch Operations ===

    /**
     * Batch insert với batch size tùy chỉnh
     *
     * @return int[][] - Mảng 2 chiều: [batch_index][row_affected]
     */
    public <T> int[][] insertBatch(String sql, List<T> data, int batchSize, BiConsumer<PreparedStatement, T> setter) {
        return batchExec(sql, data, batchSize, setter);
    }

    /**
     * Batch update với batch size tùy chỉnh
     *
     * @return int[][] - Mảng 2 chiều: [batch_index][row_affected]
     */
    public <T> int[][] updateBatch(String sql, List<T> data, int batchSize, BiConsumer<PreparedStatement, T> setter) {
        return batchExec(sql, data, batchSize, setter);
    }

    /**
     * Batch delete với batch size tùy chỉnh
     *
     * @return int[][] - Mảng 2 chiều: [batch_index][row_affected]
     */
    public <T> int[][] deleteBatch(String sql, List<T> data, int batchSize, BiConsumer<PreparedStatement, T> setter) {
        return batchExec(sql, data, batchSize, setter);
    }

    /**
     * Execute batch operation
     *
     * @return int[][] - [batch_index][row_affected]
     * Ví dụ: [[1,1,1], [1,1]] nghĩa là 2 batches, batch 1 có 3 rows, batch 2 có 2 rows
     */
    private <T> int[][] batchExec(String sql, List<T> data, int batchSize, BiConsumer<PreparedStatement, T> setter) {
        Objects.requireNonNull(data, "Data cannot be null");
        Objects.requireNonNull(setter, "Setter cannot be null");

        if (data.isEmpty()) {
            return new int[0][0];
        }

        // Spring's batchUpdate internally handles batching, just call it once
        return simpleJdbcTemplate.batchUpdate(sql, data, batchSize, setter::accept);
    }

    public static class GenericRowMapper<T> implements RowMapper<T> {

        private final Class<T> clazz;

        public GenericRowMapper(Class<T> clazz) {
            this.clazz = clazz;
        }

        public static <T> GenericRowMapper<T> of(Class<T> clazz) {
            return new GenericRowMapper<>(clazz);
        }

        @Override
        @SneakyThrows
        public T mapRow(ResultSet rs, int rowNum) {
            // Create an instance of the target class
            T entity = clazz.getDeclaredConstructor().newInstance();

            // Get metadata about the result set
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Iterate through each column in the result set
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i).toLowerCase(); // Normalize column name
                Object columnValue = rs.getObject(i);

                Field[] fields = clazz.getDeclaredFields();
                Field field = ArrayUtils.findFirst(fields, f -> f.getName().equalsIgnoreCase(columnName));
                if (field != null) {
                    field.setAccessible(true);      // Allow access to private fields
                    field.set(entity, columnValue); // Set the value
                    field.setAccessible(false);     // Close access private field
                }
            }

            return entity;
        }
    }

    /**
     * Factory class tạo RowMapper từ SQL query hoặc Class<T>
     */
    public static class RowMapperFactory {

        /**
         * Tạo RowMapper từ Class<T> - tự động map field name với column name
         * Hỗ trợ annotation @ColumnName để override tên cột
         *
         * @param clazz Class cần map
         * @return RowMapper<T>
         */
        public static <T> RowMapper<T> fromClass(Class<T> clazz) {
            return (rs, rowNum) -> {
                try {
                    T instance = clazz.getDeclaredConstructor().newInstance();
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    // Build map: columnName -> value
                    Map<String, Object> columnMap = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String col = meta.getColumnLabel(i).toLowerCase();
                        columnMap.put(col, rs.getObject(i));
                    }

                    for (Field field : getAllFields(clazz)) {
                        field.setAccessible(true);

                        String columnName = toSnakeCase(field.getName());
                        Object value = columnMap.get(columnName);
                        if (value == null) {
                            value = columnMap.get(field.getName().toLowerCase());
                        }

                        if (value != null) {
                            field.set(instance, convertValue(value, field.getType()));
                        }
                    }
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }

        /**
         * Tạo RowMapper từ SQL query string
         * Parse tên cột trong SELECT và map tương ứng vào Class<T>
         *
         * @param query SQL query (phải có dạng SELECT col1, col2 FROM ...)
         * @param clazz Class cần map
         * @return RowMapper<T>
         */
        public static <T> RowMapper<T> fromQuery(String query, Class<T> clazz) {
            List<String> columns = parseColumnsFromQuery(query);
            return (rs, rowNum) -> {
                try {
                    T instance = clazz.getDeclaredConstructor().newInstance();
                    for (String col : columns) {
                        String fieldName = toCamelCase(col.trim());

                        Field field = findField(clazz, fieldName);
                        if (field == null) continue;
                        field.setAccessible(true);
                        Object value = rs.getObject(col.trim());
                        if (value != null) {
                            field.set(instance, convertValue(value, field.getType()));
                        }
                    }
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }

        /**
         * Tạo RowMapper trả về Map<String, Object> - dùng khi không có class cụ thể
         */
        public static RowMapper<Map<String, Object>> toMap() {
            return (rs, rowNum) -> {
                ResultSetMetaData meta = rs.getMetaData();
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                return row;
            };
        }

        // ======================== PRIVATE HELPERS ========================

        private static List<String> parseColumnsFromQuery(String query) {
            String upper = query.trim().toUpperCase();
            int selectIdx = upper.indexOf("SELECT") + 6;
            int fromIdx = upper.indexOf(" FROM ");
            if (fromIdx < 0) return Collections.emptyList();

            String colPart = query.substring(selectIdx, fromIdx).trim();
            if (colPart.equals("*")) return Collections.emptyList();

            List<String> cols = new ArrayList<>();
            for (String col : colPart.split(",")) {
                String c = col.trim();
                // Xử lý alias: "table.column AS alias" -> "alias"
                if (c.toUpperCase().contains(" AS ")) {
                    c = c.substring(c.toUpperCase().lastIndexOf(" AS ") + 4).trim();
                } else if (c.contains(".")) {
                    c = c.substring(c.lastIndexOf(".") + 1).trim();
                }
                cols.add(c);
            }
            return cols;
        }

        private static String toSnakeCase(String camel) {
            return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        }

        private static String toCamelCase(String snake) {
            StringBuilder sb = new StringBuilder();
            boolean nextUpper = false;
            for (char c : snake.toLowerCase().toCharArray()) {
                if (c == '_') {
                    nextUpper = true;
                } else if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else sb.append(c);
            }
            return sb.toString();
        }

        private static List<Field> getAllFields(Class<?> clazz) {
            List<Field> fields = new ArrayList<>();
            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                fields.addAll(Arrays.asList(current.getDeclaredFields()));
                current = current.getSuperclass();
            }
            return fields;
        }

        private static <T> Field findField(Class<T> clazz, String name) {
            for (Field f : getAllFields(clazz)) {
                if (f.getName().equalsIgnoreCase(name)) return f;
            }
            return null;
        }

        private static Object convertValue(Object value, Class<?> targetType) {
            if (value == null) return null;
            if (targetType.isAssignableFrom(value.getClass())) return value;
            String str = value.toString();
            if (targetType == Long.class || targetType == long.class)
                return Long.parseLong(str);
            if (targetType == Integer.class || targetType == int.class)
                return Integer.parseInt(str);
            if (targetType == Double.class || targetType == double.class)
                return Double.parseDouble(str);
            if (targetType == Boolean.class || targetType == boolean.class)
                return Boolean.parseBoolean(str);
            if (targetType == String.class) return str;
            return value;
        }
    }
}
