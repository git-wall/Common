```java
@RestController
public class HybridController {
    
    @Autowired
    private JsonRequestValidator validator;
    
    // ✅ Simple endpoints - dùng @RequestBody @Valid
    @PostMapping("/users/simple")
    public ResponseEntity<?> simpleCreate(@RequestBody @Valid SimpleUserRequest req) {
        User user = userService.createSimple(req);
        return ResponseEntity.ok(user);
    }
    
    // ✅ Complex/high-traffic endpoints - manual validation
    @PostMapping("/orders/create")
    public ResponseEntity<?> complexCreate(HttpServletRequest request) throws IOException {
        ValidationResult<CreateOrderRequest> result = validator.validateAndParse(
            request.getInputStream(),
            CreateOrderRequest.class,
            FieldValidators.required("userId"),
            FieldValidators.positive("userId"),
            // ... more complex rules
        );
        
        if (!result.isValid()) {
            return ResponseEntity.badRequest().body(Map.of("error", result.getError()));
        }
        
        Order order = orderService.create(result.getData());
        return ResponseEntity.ok(order);
    }
}
```