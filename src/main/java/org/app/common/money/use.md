### Library for Money

```groovy
implementation 'org.javamoney:moneta:1.4.5'
```

or can use like this 

```java
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        // Create Money instances using the Monetary class
        Money euro = Monetary.EUR(new BigDecimal("100.50"));
        Money usd = Monetary.USD(new BigDecimal("120.75"));

        // Perform operations using the Money class
        Money sum = euro.add(Monetary.EUR(new BigDecimal("50.25")));
        Money difference = usd.subtract(Monetary.USD(new BigDecimal("20.75")));
        Money multiplied = euro.multiply(new BigDecimal("2"));

        // Print results
        System.out.println("Euro: " + euro);
        System.out.println("USD: " + usd);
        System.out.println("Sum (EUR): " + sum);
        System.out.println("Difference (USD): " + difference);
        System.out.println("Multiplied (EUR): " + multiplied);
    }
}

//Output:
//Euro: 100.500 EUR
//USD: 120.750 USD
//Sum (EUR): 150.750 EUR
//Difference (USD): 100.000 USD
//Multiplied (EUR): 201.000 EUR
```