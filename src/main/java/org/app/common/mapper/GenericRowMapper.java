package org.app.common.mapper;

import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;

public class GenericRowMapper<T> implements RowMapper<T> {

    private final Class<T> clazz;

    public GenericRowMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    @NonNull
    public static <T> GenericRowMapper<T> of(Class<T> clazz) {
        return new GenericRowMapper<>(clazz);
    }

    @Override
    @SneakyThrows
    public T mapRow(@NonNull ResultSet rs, int rowNum) {
        // Create an instance of the target class
        T entity = clazz.getDeclaredConstructor().newInstance();

        // Get metadata about the result set
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Iterate through each column in the result set
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i).toLowerCase(); // Normalize column name
            Object columnValue = rs.getObject(i);

            // Find the corresponding field in the entity class
            Field field = findField(clazz, columnName);
            if (field != null) {
                field.setAccessible(true);      // Allow access to private fields
                field.set(entity, columnValue); // Set the value
                field.setAccessible(false);     // Close access private field
            }
        }

        return entity;
    }

    /**
     * Finds a field in the given class by matching its name (case-insensitive).
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Field[] fields = clazz.getDeclaredFields();
        return Arrays.stream(fields)
                .filter(field -> field.getName().equalsIgnoreCase(fieldName))
                .findFirst()
                .orElse(null);
    }
}
