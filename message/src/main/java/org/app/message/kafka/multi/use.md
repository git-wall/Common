```java
public class Topic1MessageProcessor implements MessageProcessor<String, String> {
    @Override
    void process(ConsumerRecord<K, V> record) throws Exception{
        // do
    }
}

MultiKafkaService.runConsumer("broker1", "topic1", Topic1MessageProcessor);
MultiKafkaService.runConsumerNoRetry("broker1", "topic1", Topic1MessageProcessor);
MultiKafkaService.producerSend("broker1", "topic1", "key", "value");
KafkaProducer<String, String> producer = MultiKafkaService.getProducer("broker1");
```

```yaml
kafka:
  brokers:
    configs:
      broker1:
        bootstrap-servers: localhost:9092
        group-id: group1
        type: BOTH
        producer:
          acks: all
          retries: 3
          batch-size: 16384
          linger-ms: 1
          buffer-memory: 33554432
          compression-type: snappy
        consumer:
          max-concurrency: 8
          enable-auto-commit: false
          auto-offset-reset: earliest
          max-poll-records: 500

      broker2:
        bootstrap-servers: localhost:9093
        type: PRODUCER_ONLY
        producer:
          acks: all
          compression-type: lz4

      broker3:
        bootstrap-servers: localhost:9094
        group-id: group3
        type: CONSUMER_ONLY
        consumer:
          enable-auto-commit: false
```