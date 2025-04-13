```java
@Component
public class OrderService {

    @Workflow(value = "order-flow", order = 1)
    public void step1() {
        System.out.println("Step 1: Validate Order");
    }

    @Workflow(value = "order-flow", order = 2)
    public void step2() {
        System.out.println("Step 2: Reserve Inventory");
    }

    @Workflow(value = "order-flow", order = 3)
    public void step3() {
        System.out.println("Step 3: Notify Shipping");
    }
}
```