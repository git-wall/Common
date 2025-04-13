### JPA Optimistic Locking
Use case: 
- Two users trying to update the price of the same product at the same time.
- Only one is successful
- the other gets an exception OptimisticLockException.
```java
import javax.persistence.Version;

class Product {
    private Long id;
    private String name;
    private double price;
    private int quantity;
    private int stock;
    @Version
    private Long version;
    // getters and setters
}
```

### Pessimistic Locking in JPA
Use case:
- Multiple threads are trying to update the stock.
- Other threads are trying to wait look is released by the first thread that acquired it.
```java
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.name = ?")
    List<Product> findByName(String name);
}

@Transactional
public void updateStock(Product product) {
    Product p = productRepository.findByName(product.getName());
    p.setQuantity(p.getQuantity() + p.getStock());
    productRepository.save(p);
}
```

### Isolation Levels in JPA
Use case:
- Concurrent money transfer between two accounts.
- Transaction are serializable, preventing inconsistent account balances.
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void transfer(){
    // logic here
}
```

### Retry
```java
import org.springframework.retry.annotation.Retryable;

@Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 2)
@Transactional
public void transfer() {
    // logic here
}
```

### Database - level Constraints
```sql
ALTER TABLE product ADD CONSTRAINT product_name_unique UNIQUE (name);
```

