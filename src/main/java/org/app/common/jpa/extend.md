### JPA Entity Graphs
```java
@Entity
@NamedEntityGraph(
    name = "Order.withCustomerAndItems",
    attributeNodes = {
        @NamedAttributeNode("customer"),
        @NamedAttributeNode("items")
    }
)
public class Order {
    
}

// EntityGraphType.FETCH overrides the default behavior of EntityGraphType.LOAD
// EntityGraphType.LOAD is additional any fetches that are needed to load the entity
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(value = "Order.withCustomerAndItems", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Order> findById(Long id);
    // or
    @EntityGraph(attributePaths = {"customer", "items"})
    Optional<Order> findById(Long id);
}
```
### Define JSONB Column
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "payload", columnDefinition = "jsonb")
private String payload;
```