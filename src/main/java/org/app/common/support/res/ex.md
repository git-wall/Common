```java
@RestController
@RequestMapping("/api")
public class CleanController {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository itemRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private DirectJsonResponse jsonResponse;
    
    /**
     * ✅ Pattern: Fetch data → validate → write JSON
     */
    @GetMapping("/orders/{id}")
    public void getOrder(@PathVariable Long id, HttpServletResponse response) 
            throws IOException {
        
        // Phase 1: Fetch & validate data - có thể throw exception
        try {
            Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
            
            List<OrderItem> items = itemRepository.findByOrderId(id);
            
            Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));
            
            // Phase 2: All data OK → write JSON response
            // ✅ Từ đây không còn business logic, chỉ serialize
            jsonResponse.writeObject(response, json -> {
                json.writeNumberField("orderId", order.getId());
                json.writeNumberField("total", order.getTotal().doubleValue());
                json.writeStringField("status", order.getStatus());
                
                json.writeObject("customer", 
                    customerToJson(customer));
                
                json.writeArray("items", items, 
                    this::orderItemToJson);
            });
            
        } catch (Exception e) {
            // ✅ Chưa write response → Controller Advice xử lý
            throw e;
        }
    }
    
    /**
     * ✅ Mapping functions - reusable, testable
     */
    private void customerToJson(JsonWriter json, Customer customer) throws IOException {
        json.writeNumberField("id", customer.getId());
        json.writeStringField("name", customer.getName());
        json.writeStringField("email", customer.getEmail());
    }
    
    private void orderItemToJson(JsonWriter json, OrderItem item) throws IOException {
        json.writeNumberField("productId", item.getProductId());
        json.writeStringField("productName", item.getProductName());
        json.writeNumberField("quantity", item.getQuantity());
        json.writeNumberField("price", item.getPrice().doubleValue());
    }
}
```