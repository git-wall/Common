```java
class DataTableExample {
    public static void main(String[] args) {
        // Create a DataTable using builder pattern
        DataTable employeeTable = DataTable.builder()
            .addColumn("ID", Integer.class, false)
            .addColumn("Name", String.class, false)
            .addColumn("Age", Integer.class)
            .addColumn("Salary", Double.class)
            .addColumn("Department", String.class)
            .build();
        
        employeeTable.setTableName("Employees");
        
        // Add rows using different methods
        employeeTable.addRow(1, "John Doe", 30, 75000.0, "IT");
        employeeTable.addRow(2, "Jane Smith", 28, 65000.0, "HR");
        employeeTable.addRow(3, "Bob Johnson", 35, 80000.0, "IT");
        
        // Add row using Row object
        DataTable.Row newRow = employeeTable.newRow();
        newRow.set("ID", 4);
        newRow.set("Name", "Alice Brown");
        newRow.set("Age", 32);
        newRow.set("Salary", 70000.0);
        newRow.set("Department", "Finance");
        employeeTable.addRow(newRow);
        
        // Display table
        System.out.println("Original Table:");
        employeeTable.printTable();
        
        // Query operations
        System.out.println("\nIT Department employees:");
        List<DataTable.Row> itEmployees = employeeTable.select(
            row -> "IT".equals(row.get("Department"))
        );
        itEmployees.forEach(System.out::println);
        
        System.out.println("\nEmployees with salary > 70000:");
        DataTable highSalaryTable = employeeTable.where(
            row -> {
                Double salary = row.get("Salary", Double.class);
                return salary != null && salary > 70000.0;
            }
        );
        highSalaryTable.printTable();
        
        // Type-safe access
        System.out.println("\nType-safe access example:");
        DataTable.Row firstRow = employeeTable.getRow(0);
        Integer id = firstRow.get("ID", Integer.class);
        String name = firstRow.get("Name", String.class);
        System.out.println("Employee ID: " + id + ", Name: " + name);
        
        // Modify data
        firstRow.set("Age", 31);
        System.out.println("Updated age: " + firstRow.get("Age"));
        
        System.out.println("\nTable info:");
        System.out.println("Column count: " + employeeTable.getColumnCount());
        System.out.println("Row count: " + employeeTable.getRowCount());
        System.out.println("Column names: " + employeeTable.getColumnNames());
    }
}
```