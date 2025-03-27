```properties
# acks: Controls the number of acknowledgments the producer requires before considering a request complete.
#   - acks=all (most reliable, but slower)
#   - acks=1 (default, balanced)
#   - acks=0 (fastest, but least reliable)
spring.kafka.producer.acks=all

#linger.ms: Delay in milliseconds to wait for additional messages before sending a batch.
#Increase this to improve throughput at the cost of latency.
spring.kafka.producer.linger.ms=20

#batch.size: Maximum size of a batch of messages in bytes.
#Increase this to improve throughput, but ensure it doesn't exceed the broker's message.max.bytes.
spring.kafka.producer.batch.size=16384

#buffer.memory: Total memory available for buffering unsent messages.
#Increase this if you have high throughput or large messages.
spring.kafka.producer.buffer.memory=33554432

#compression.type: Compress messages to reduce network bandwidth usage.
#Options: none, gzip, snappy, lz4, zstd.
spring.kafka.producer.compression.type=snappy

spring.kafka.producer.retries=3

#retry.backoff.ms: Delay between retries.
spring.kafka.producer.retry.backoff.ms=100

#enable.idempotence: Ensures exactly-once delivery by preventing duplicate messages.
#Requires acks=all and max.in.flight.requests.per.connection=1.
spring.kafka.producer.enable.idempotence=true

#max.in.flight.requests.per.connection: Maximum number of unacknowledged requests the producer can send.
#Increase this for higher throughput, but ensure retries is set to avoid message reordering
spring.kafka.producer.max.in.flight.requests.per.connection=5
```