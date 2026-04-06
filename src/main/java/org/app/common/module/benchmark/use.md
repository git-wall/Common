### Enable AOP

```java
@SpringBootApplication
@EnableAspectJAutoProxy
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

```java
@RestController
public class OrderController {
    
    @ApiProfiled("OrderAPI-Old")
    @PostMapping("/order/old")
    public Order createOrder_Old(@RequestBody OrderRequest req) {
        return orderService.createOld(req);
    }
    
    @ApiProfiled("OrderAPI-New")
    @PostMapping("/order/new")
    public Order createOrder_New(@RequestBody OrderRequest req) {
        return orderService.createNew(req);
    }
}
```

Cấu trúc file sau các lần gọi khi bật app
```yml
├── OrderAPI/
│   ├── v1/
│   │   ├── avg.json
│   │   ├── min.json
│   │   ├── max.json
│   │   └── metadata.json
│   ├── v2/
│   │   └── ... (4 files tương tự)
│   └── v3/
│       └── ... (4 files tương tự)
```