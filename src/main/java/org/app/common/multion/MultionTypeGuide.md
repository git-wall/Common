# MultionType Usage Guide

MultionType is a powerful pattern that maps enum constants to their corresponding implementation classes. This allows for a type-safe, enum-based factory pattern that integrates with Spring's dependency injection.

## Core Concepts

- **MultionType<K, V>**: Maps enum constants (K) to implementation instances (V)
- **MultionManager**: Singleton manager that maintains MultionType instances
- **Automatic Implementation Discovery**: Automatically finds and instantiates implementations based on naming conventions

## Basic Usage

### 1. Define an Interface

First, define an interface that your implementations will implement:

```java
public interface PaymentProcessor {
    void processPayment(double amount);
}
public enum PaymentType {
    CREDIT,
    DEBIT,
    PAYPAL,
    CRYPTO
}
public class CreditPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing credit payment of $" + amount);
    }
}

public class DebitPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing debit payment of $" + amount);
    }
}

public class PaypalPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing PayPal payment of $" + amount);
    }
}

public class CryptoPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing cryptocurrency payment of $" + amount);
    }
}
// Get the MultionType instance
MultionType<PaymentType, PaymentProcessor> paymentProcessors = 
    MultionType.of(PaymentType.class, PaymentProcessor.class);

// Get a specific implementation
PaymentProcessor creditProcessor = paymentProcessors.get(PaymentType.CREDIT);

// Use the implementation
creditProcessor.processPayment(100.00);


// Create a custom MultionType
MultionType<PaymentType, PaymentProcessor> customProcessors = 
    new MultionType<>(PaymentType.class, PaymentProcessor.class);

// Register with the MultionManager
MultionManager.getInstance().registerMultionType(PaymentType.class, customProcessors);
// Register with the MultionManager
Map<Class<?>, Class<?>> multionTypes = new HashMap<>();
multionTypes.put(PaymentType.class, PaymentProcessor.class);
multionTypes.put(ShippingMethod.class, ShippingProvider.class);
MultionManager.getInstance().registerMultionTypes(multionTypes);

boolean exists = MultionManager.getInstance().hasMultionType(PaymentType.class);
MultionType<PaymentType, PaymentProcessor> removed = 
    MultionManager.getInstance().removeMultionType(PaymentType.class, PaymentProcessor.class);


// PaymentService.java
@Service
public class PaymentService {
    private final MultionType<PaymentType, PaymentProcessor> paymentProcessors;
    
    public PaymentService() {
        // Initialize MultionType
        this.paymentProcessors = MultionType.of(PaymentType.class, PaymentProcessor.class);
    }
    
    public void processPayment(PaymentType type, double amount) {
        PaymentProcessor processor = paymentProcessors.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + type);
        }
        processor.processPayment(amount);
    }
}


// Usage
@RestController
public class PaymentController {
    private final PaymentService paymentService;
    
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
        paymentService.processPayment(request.getType(), request.getAmount());
        return ResponseEntity.ok().build();
    }
}
```