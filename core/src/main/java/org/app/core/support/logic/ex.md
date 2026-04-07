```java
 import org.app.common.usecase.enrich.Logic;

public List<OrderDTO> getOrders() {
  List<Order> orders = orderRepository.findAll(); // 1 query

  List<OrderDTO> orderDTOs = Logic.getChildToFillInNew(
    orders,
    Order::getCustomerId,
    customerRepository::findAllById,
    Customer::getId,
    (e, c) -> new OrderDTO(e, c)
  );
}
```