```scala 3
kafka.brokers {
  main {
    bootstrapServers = "localhost:9092"
    groupId = "main-group"
    type = "BOTH"
    producer {
      acks = "all"
      retries = 3
    }
    consumer {
      enableAutoCommit = false
      autoOffsetReset = "earliest"
      maxPollRecords = 500
    }
  }
}
```