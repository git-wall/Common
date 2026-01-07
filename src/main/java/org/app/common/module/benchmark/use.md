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

<function_calls>
<invoke name="artifacts">
<parameter name="command">update</parameter>
<parameter name="id">profile_viewer</parameter>
<parameter name="old_str">                </parameter>
<parameter name="new_str">                <strong>💡 Khuyến nghị:</strong> Ưu tiên ${bestTime.version} nếu cần speed, hoặc ${bestMem.version} nếu giới hạn memory.<br><br>
<strong>Range</strong> = Khoảng dao động (Max - Min). Số càng nhỏ = performance ổn định.
`;
}
document.getElementById('recommendationNote').innerHTML = recommendation;
}
</script>
</body>
</html>