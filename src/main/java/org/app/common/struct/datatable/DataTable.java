package org.app.common.struct.datatable;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A DataTable implementation for Java similar to C#'s DataTable
 * Provides easy-to-use tabular data manipulation with type safety
 */
public class DataTable {

    // Column definition class
    @Getter
    public static class Column {
        private final String name;
        private final Class<?> type;
        private final boolean allowNull;

        public Column(String name, Class<?> type) {
            this(name, type, true);
        }

        public Column(String name, Class<?> type, boolean allowNull) {
            this.name = name;
            this.type = type;
            this.allowNull = allowNull;
        }

        @Override
        public String toString() {
            return String.format("Column{name='%s', type=%s, allowNull=%s}",
                    name, type.getSimpleName(), allowNull);
        }
    }

    // Row class for type-safe data access
    public class Row {
        private final Map<String, Object> data;
        @Getter
        private final int index;

        private Row(Map<String, Object> data, int index) {
            this.data = new LinkedHashMap<>(data);
            this.index = index;
        }

        public <T> T get(String columnName, Class<T> type) {
            validateColumn(columnName);
            Object value = data.get(columnName);

            if (value == null) {
                return null;
            }

            if (!type.isAssignableFrom(value.getClass())) {
                throw new ClassCastException(
                        String.format("Cannot cast %s to %s for column '%s'",
                                value.getClass().getSimpleName(),
                                type.getSimpleName(),
                                columnName));
            }

            return type.cast(value);
        }

        public Object get(String columnName) {
            validateColumn(columnName);
            return data.get(columnName);
        }

        public void set(String columnName, Object value) {
            validateColumn(columnName);
            Column column = columnMap.get(columnName);
            validateValue(column, value);
            data.put(columnName, value);
        }

        public Set<String> getColumnNames() {
            return new LinkedHashSet<>(data.keySet());
        }

        @Override
        public String toString() {
            return data.toString();
        }
    }

    // Builder pattern for easy DataTable creation
    public static class Builder {
        private final List<Column> columns = new ArrayList<>();

        public Builder addColumn(String name, Class<?> type) {
            columns.add(new Column(name, type));
            return this;
        }

        public Builder addColumn(String name, Class<?> type, boolean allowNull) {
            columns.add(new Column(name, type, allowNull));
            return this;
        }

        public DataTable build() {
            return new DataTable(columns);
        }
    }

    // Instance variables
    private final List<Column> columns;
    private final Map<String, Column> columnMap;
    private final List<Row> rows;
    // Basic operations
    @Getter
    @Setter
    private String tableName;

    // Private constructor - use Builder
    private DataTable(List<Column> columns) {
        this.columns = new ArrayList<>(columns);
        this.columnMap = new LinkedHashMap<>();
        this.rows = new ArrayList<>();

        // Build a column map for a quick lookup
        for (Column column : columns) {
            if (columnMap.containsKey(column.getName())) {
                throw new IllegalArgumentException("Duplicate column name: " + column.getName());
            }
            columnMap.put(column.getName(), column);
        }
    }

    // Factory method
    public static Builder builder() {
        return new Builder();
    }

    public List<Column> getColumns() {
        return new ArrayList<>(columns);
    }

    public List<String> getColumnNames() {
        return columns.stream()
                .map(Column::getName)
                .collect(Collectors.toList());
    }

    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
        return rows.size();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    // Row operations
    public Row newRow() {
        Map<String, Object> data = new LinkedHashMap<>();
        for (Column column : columns) {
            data.put(column.getName(), null);
        }
        return new Row(data, -1); // -1 indicates new row not yet added
    }

    public void addRow(Row row) {
        if (row.index != -1) {
            throw new IllegalArgumentException("Row is already part of a DataTable");
        }

        // Create new row with current index
        Map<String, Object> data = new LinkedHashMap<>();
        for (Column column : columns) {
            Object value = row.data.get(column.getName());
            validateValue(column, value);
            data.put(column.getName(), value);
        }

        rows.add(new Row(data, rows.size()));
    }

    public void addRow(Object... values) {
        if (values.length != columns.size()) {
            throw new IllegalArgumentException(
                    String.format("Expected %d values, got %d", columns.size(), values.length));
        }

        Row row = newRow();
        for (int i = 0; i < columns.size(); i++) {
            row.set(columns.get(i).getName(), values[i]);
        }
        addRow(row);
    }

    public Row getRow(int index) {
        if (index < 0 || index >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index: " + index);
        }
        return rows.get(index);
    }

    public List<Row> getRows() {
        return new ArrayList<>(rows);
    }

    public void removeRow(int index) {
        if (index < 0 || index >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index: " + index);
        }
        rows.remove(index);

        // Update row indices
        for (int i = index; i < rows.size(); i++) {
            Row row = rows.get(i);
            rows.set(i, new Row(row.data, i));
        }
    }

    public void clear() {
        rows.clear();
    }

    // Query operations
    public List<Row> select(Predicate<Row> predicate) {
        return rows.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public Row findFirst(Predicate<Row> predicate) {
        return rows.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    public DataTable where(Predicate<Row> predicate) {
        DataTable result = new DataTable(this.columns);
        result.setTableName(this.tableName + "_filtered");

        List<Row> filteredRows = select(predicate);
        for (Row row : filteredRows) {
            result.addRow(row.data.values().toArray());
        }

        return result;
    }

    // Utility methods
    private void validateColumn(String columnName) {
        if (!columnMap.containsKey(columnName)) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
    }

    private void validateValue(Column column, Object value) {
        if (value == null && !column.isAllowNull()) {
            throw new IllegalArgumentException(
                    String.format("Column '%s' does not allow null values", column.getName()));
        }

        if (value != null && !column.getType().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(
                    String.format("Value type %s is not compatible with column type %s for column '%s'",
                            value.getClass().getSimpleName(),
                            column.getType().getSimpleName(),
                            column.getName()));
        }
    }

    // Display methods
    public void printTable() {
        System.out.println(toString());
    }

    @Override
    public String toString() {
        if (rows.isEmpty()) {
            return "Empty DataTable" + (tableName != null ? " (" + tableName + ")" : "");
        }

        StringBuilder sb = new StringBuilder();
        if (tableName != null) {
            sb.append("DataTable: ").append(tableName).append("\n");
        }

        // Calculate column widths
        Map<String, Integer> columnWidths = new LinkedHashMap<>();
        for (Column column : columns) {
            int maxWidth = column.getName().length();
            for (Row row : rows) {
                Object value = row.get(column.getName());
                String strValue = value != null ? value.toString() : "null";
                maxWidth = Math.max(maxWidth, strValue.length());
            }
            columnWidths.put(column.getName(), Math.max(maxWidth, 8));
        }

        // Header
        sb.append("|");
        for (Column column : columns) {
            String name = column.getName();
            int width = columnWidths.get(name);
            sb.append(String.format(" %-" + width + "s |", name));
        }
        sb.append("\n");

        // Separator
        sb.append("|");
        for (Column column : columns) {
            int width = columnWidths.get(column.getName());
            sb.append("-".repeat(width + 2)).append("|");
        }
        sb.append("\n");

        // Data rows
        for (Row row : rows) {
            sb.append("|");
            for (Column column : columns) {
                Object value = row.get(column.getName());
                String strValue = value != null ? value.toString() : "null";
                int width = columnWidths.get(column.getName());
                sb.append(String.format(" %-" + width + "s |", strValue));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}