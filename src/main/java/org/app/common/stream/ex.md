```java
// Simple filtering and sorting
List<Person> result = DataStream.FROM(people)
                .WHERE(p -> p.getAge() > 25)
                .ORDER_BY(Person::getName)
                .LIMIT(3)
                .toList();

// Complex grouping with conditions
var groups = DataStream.FROM(people)
        .GROUP_BY(Person::getDepartment)
        .HAVING(group -> group.count() > 1)
        .toList();

// Condition-based filtering
DataStream.

FROM(people)
    .

WHERE("age",Condition.GREATER_THAN(25))
        .

WHERE("department",Condition.IN(Arrays.asList("Engineering", "Sales")))
        .

toList();

// Aggregations
double avgAge = DataStream.FROM(people)
        .WHERE("department", Condition.EQUALS("Engineering"))
        .AVG(Person::getAge)
        .orElse(0.0);
```

--- Mega Stream

```java
        // Initialize complex sample datasets
List<Employee> employees = createEmployees();

// MEGA COMPLEX QUERY: One massive flow demonstrating ALL features
var ultimateResult = DataStream.FROM(employees)
        // 1. FILTERING: Multiple WHERE conditions with different operators
        .WHERE(emp -> emp.getAge() >= 25)
        .WHERE("isActive", Condition.EQUALS(true))
        .WHERE("salary", Condition.GREATER_THAN(50000))
        .WHERE("department", Condition.IN(Arrays.asList("Engineering", "Sales", "Marketing")))
        .WHERE("name", Condition.NOT_LIKE("Test"))

        // 2. DATA MANIPULATION: Insert new employees
        .INSERT(new Employee("NewHire1", 26, "Engineering", 55000, true, "Senior"))
        .INSERT(Arrays.asList(
                new Employee("NewHire2", 29, "Sales", 48000, true, "Junior"),
                new Employee("NewHire3", 31, "Marketing", 52000, true, "Mid")
        ))

        // 3. UPDATE: Modify existing data
        .UPDATE(
                emp -> emp.getName().startsWith("John"),
                emp -> new Employee(emp.getName(), emp.getAge(), emp.getDepartment(),
                        emp.getSalary() + 5000, emp.isActive(), "Promoted")
        )

        // 4. JOIN: Complex multi-table join with departments
        .JOIN(departments,
                (emp, dept) -> emp.getDepartment().equals(dept.getName()),
                (emp, dept) -> new EmployeeDepartmentView(
                        emp.getName(), emp.getAge(), emp.getSalary(),
                        dept.getName(), dept.getBudget(), dept.getLocation()
                ))

        // 5. FILTERING AFTER JOIN: Filter on joined data
        .WHERE(view -> view.getDepartmentBudget() > 100000)
        .WHERE("location", Condition.NOT_EQUALS("Remote"))

        // 6. LEFT JOIN: Add project information
        .LEFT_JOIN(projects,
                (view, project) -> view.getDepartmentName().equals(project.getDepartment()),
                (view, projectOpt) -> new CompleteEmployeeView(
                        view.getName(), view.getAge(), view.getSalary(),
                        view.getDepartmentName(), view.getDepartmentBudget(), view.getLocation(),
                        projectOpt.map(Project::getName).orElse("No Project"),
                        projectOpt.map(Project::getStatus).orElse("N/A"),
                        projectOpt.map(Project::getBudget).orElse(0)
                ))

        // 7. COMPLEX GROUPING: Group by multiple criteria
        .GROUP_BY(view -> view.getDepartmentName() + "-" + view.getProjectStatus())

        // 8. HAVING: Filter groups based on aggregate conditions
        .HAVING(group -> group.count() >= 2)
        .HAVING(group -> {
            double avgSalary = group.getItems().stream()
                    .mapToDouble(CompleteEmployeeView::getSalary)
                    .average().orElse(0);
            return avgSalary > 55000;
        })

        // 9. TRANSFORM: Convert groups to summary objects
        .SELECT(group -> new DepartmentProjectSummary(
                group.getKey(),
                group.count(),
                group.getItems().stream().mapToDouble(CompleteEmployeeView::getSalary).sum(),
                group.getItems().stream().mapToDouble(CompleteEmployeeView::getSalary).average().orElse(0),
                group.getItems().stream().mapToInt(CompleteEmployeeView::getAge).min().orElse(0),
                group.getItems().stream().mapToInt(CompleteEmployeeView::getAge).max().orElse(0),
                group.getItems().stream().mapToDouble(CompleteEmployeeView::getProjectBudget).sum()
        ))

        // 10. UNION: Combine with additional computed data
        .UNION(DataStream.FROM(createAdditionalSummaries()))

        // 11. DISTINCT: Remove duplicates based on key
        .DISTINCT(summary -> summary.getGroupKey())

        // 12. SORTING: Multi-level sorting
        .ORDER_BY(DepartmentProjectSummary::getTotalSalary, SortDirection.DESC)
        .ORDER_BY(DepartmentProjectSummary::getEmployeeCount, SortDirection.DESC)

        // 13. PAGINATION: Skip and limit
        .OFFSET(0)
        .LIMIT(10)

        // 14. FINAL EXECUTION
        .toList();
```