```properties
# Kafka Bootstrap Servers
spring.kafka.bootstrap-servers=localhost:9092
 
# Consumer Configuration
spring.kafka.consumer.max-poll-records=1000
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.heartbeat-interval=3000

spring.kafka.consumer.fetch-min-size=1MB
spring.kafka.consumer.fetch-max-wait=500ms

# Properties accessed via getProperties()
spring.kafka.consumer.properties.max.partition.fetch-bytes=1048576
spring.kafka.consumer.properties.max.poll.interval.ms=300000
spring.kafka.consumer.properties.session.timeout.ms=10000
spring.kafka.consumer.properties.retry.backoff.ms=1000
spring.kafka.consumer.properties.connections.max.idle.ms=540000
spring.kafka.consumer.properties.request.timeout.ms=30000

# Listener Configuration
spring.kafka.listener.concurrency=5
spring.kafka.listener.ack-mode=MANUAL
spring.kafka.listener.poll-timeout=3000
```

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all
      retries: 3
      batch-size: 16384
      buffer-memory: 33554432
      compression-type: snappy
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
    consumer:
      auto-offset-reset: earliest
      enable-auto-commit: false
      max-poll-records: 500
      properties:
        isolation.level: read_committed
    listener:
      ack-mode: MANUAL
      concurrency: 3
      poll-timeout: 3000
    retry:
      enabled: true
      max-attempts: 3
      initial-interval: 1000
      multiplier: 2.0
      max-interval: 10000
      retry-topic-suffix: -retry
      dlt-topic-suffix: -dlt
```

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderHandler extends AbstractMessageHandler<String> {
    
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doHandle(String message) throws Exception {
        Order order = objectMapper.readValue(message, Order.class);
        orderService.processOrder(order);
    }

    @Override
    protected void doRetry(String message) throws Exception {
        log.info("Retrying message: {}", message);
        doHandle(message);
    }

    @Override
    protected void doDlt(String message) throws Exception {
        log.error("Message sent to DLT: {}", message);
        // Implement DLT logic (e.g., save to DB, send notification)
        orderService.handleFailedOrder(message);
    }
}
```