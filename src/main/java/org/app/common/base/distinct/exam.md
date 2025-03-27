```java
public class Person implements GenericComparable {
    private String name;
    private int age;
    private String city;
    @ExcludeFromComparison
    private double salary; // This field won't be considered in comparison

    public Person(String name, int age, String city, double salary) {
        this.name = name;
        this.age = age;
        this.city = city;
        this.salary = salary;
    }

    @Override
    public boolean equals(Object o) {
        return equalsGeneric(o);
    }

    @Override
    public int hashCode() {
        return hashCodeGeneric();
    }
}
```