```java
public static void main(String[] args) {
        // Example usage of the Inning class
        String testValue = "";

        String finalResult = CHECK.ifNullThrow(testValue, "Value cannot be null")
                .ifNullSkipp(testValue, value -> System.out.println("Value is: " + value))
                .ifNullReturn(testValue, "null");
        
        System.out.println(finalResult);
    }
```