# Thiết kế Kafka Cluster cho nhiều mục đích sử dụng

| Cụm                       | Mục đích                              | Đặc điểm                              | Mục tiêu tối ưu         |
|---------------------------|---------------------------------------|---------------------------------------|-------------------------|
| **Cluster 1 – Log**       | Thu log, audit, trace                 | Dữ liệu lớn, latency không quan trọng | Dung lượng & throughput |
| **Cluster 2 – Business**  | Event bus cho service                 | Message nhỏ, yêu cầu latency thấp     | Độ trễ & ổn định        |
| **Cluster 3 – Analytics** | Đồng bộ dữ liệu ra hệ thống phân tích | Throughput cao, xử lý batch           | I/O & dung lượng        |

## Cấu hình Broker

### Công thức tính dung lượng & số broker

Giả sử bạn đã có:
* R_day: lượng dữ liệu ghi mỗi ngày (GB)
* T: số ngày lưu (retention)
* RF: replication factor (2 hoặc 3)
* OH: overhead (~1.1)
* Disk_usable: dung lượng usable mỗi broker (TB)

Total Size = R_day x T X RF X OH
Broker count = Total Size / Disk_usable

### Lựa chọn dung lượng ổ đĩa
Dưới đây là các gợi ý về dung lượng ổ đĩa cho từng cụm Kafka:
Ít hơn (500GB - 1TB): 
  Chỉ nên dùng cho môi trường thử nghiệm, phát triển (dev/staging) hoặc các hệ thống sản xuất có lưu lượng cực kỳ thấp.
Nhiều hơn (8TB - 16TB+): 
  Khi bạn có khối lượng dữ liệu khổng lồ (như ví dụ 20TB/ngày của bạn) và cần giảm số lượng máy chủ vật lý. 
  Tuy nhiên, ở mức này, người ta thường chuyển sang dùng ổ NVMe thay vì SSD SATA/SAS 
  và thường dùng nhiều ổ gộp lại để tăng tổng băng thông I/O, không chỉ dựa vào 1 ổ đơn lẻ.

### Kích thước tiêu chuẩn cho từng broker
- tùy vào nhu cầu muốn quản lý ít hay nhiều broker mà chọn cấu hình phù hợp

| Mức độ cluster | Brokers | CPU/Broker | RAM/Broker | Disk                   | Network  |
| -------------- | ------- | ---------- | ---------- | ---------------------- | -------- |
| **Small**      | 3       | 4 vCPU     | 8 GB       | 200–500 GB (SSD)       | 1 Gbps   |
| **Medium**     | 3–5     | 8 vCPU     | 16 GB      | 500 GB–1 TB (SSD/NVMe) | 10 Gbps  |
| **Large**      | 5–9     | 16 vCPU    | 32 GB      | 1–2 TB NVMe            | 10 Gbps+ |

### Cấu hình gợi ý cho từng cụm

#### Cluster 1 – Log
| Thông số                    | Gợi ý                                        |
|-----------------------------|----------------------------------------------|
| **Replication factor (RF)** | 2                                            |
| **Retention.ms**            | 7–30 ngày                                    |
| **Log segment bytes**       | 1 GB – 2 GB                                  |
| **num.partitions/topic**    | 3–6 (nên theo app consumer chia cho hợp lsy) |
| **Broker RAM**              | 16–32 GB                                     |
| **Disk**                    | HDD 2–4 TB mỗi broker                        |
| **CPU**                     | 8 core                                       |
| **Compression**             | `Snappy`, `LZ4`, `zstd`                      |
| **Network**                 | 1–10 Gbps                                    |
| **Example**                 | 20 TB data, RF=2, disk=2 TB → ~5 brokers     |

Example:
- R_day = 10 TB/day
- T = 7 days
- RF = 2
- OH = 1.1
- Disk_usable = 8 TB/broker

Calculation:
- Total Size = 10 x 7 x 2 x 1.1 = 150 TB
- Broker count = ceil(150 / 2) = 75 brokers

Tổng tài nguyên cụm:
- CPU: 8 core x 75 broker = 600 cores
- RAM: 32 GB x 75 broker = 2400 GB
- Disk: 2 TB x 75 broker = 150 TB

Nếu muốn quản lý ít broker hơn, có thể tăng dung lượng ổ đĩa lên 4 TB hoặc 8 TB mỗi broker.
- CPU: 32 core x 20 = 640 cores
- RAM: 128 GB x 20 = 2560 GB
- Disk: 8 TB x 20 = 160 TB

___
#### Cluster 2 – Business
| Thông số                                | Gợi ý                 |
|-----------------------------------------|-----------------------|
| **Replication factor (RF)**             | 3                     |
| **Retention.ms**                        | 1–7 ngày              |
| **Log segment bytes**                   | 512 MB – 1 GB         |
| **num.partitions/topic**                | 6–12 (tùy throughput) |
| **Broker RAM**                          | 32–64 GB              |
| **Disk**                                | SSD 1–2 TB mỗi broker |
| **CPU**                                 | 8–16 core             |
| **Compression**                         | LZ4                   |
| **Network**                             | ≥10 Gbps              |
| - `num.network.threads=6–8`             |                       |
| - `num.io.threads=8–16`                 |                       |
| - `socket.send.buffer.bytes=1048576`    |                       |
| - `socket.receive.buffer.bytes=1048576` |                       |
| - `socket.request.max.bytes=104857600`  |                       |

Example:
- R_day = 2 TB/day
- T = 3 days
- RF = 2
- OH = 1.1
- Disk_usable = 2 TB/broker

Calculation:
- Total Size = 2 x 3 x 2 x 1.1 = 13.2 TB
- Broker count = ceil(13.2 / 4) = ~4 brokers

___
#### Cluster 3 – Analytics
| Thông số                    | Gợi ý                                           |
|-----------------------------|-------------------------------------------------|
| **Replication factor (RF)** | 2 hoặc 3                                        |
| **Retention.ms**            | 1–3 ngày                                        |
| **Log segment bytes**       | 2–4 GB                                          |
| **num.partitions/topic**    | 3–6                                             |
| **Broker RAM**              | 16–32 GB                                        |
| **Disk**                    | HDD 4–8 TB mỗi broker                           |
| **CPU**                     | 8 core                                          |
| **Compression**             | zstd hoặc LZ4                                   |
| **Note**                    | Có thể dùng MirrorMaker để ingest từ `business` |

Example : cái này lười quá tự tính nhé =))

___

### Producer config (Spring Boot / service side)

| Config                                  | Mô tả                         | Giá trị gợi ý                                   |
|-----------------------------------------|-------------------------------|-------------------------------------------------|
| `acks`                                  | Độ tin cậy gửi message        | `1` (log), `all` (business)                     |
| `compression.type`                      | Nén dữ liệu                   | `snappy` (log), `lz4` (biz), `zstd` (analytics) |
| `linger.ms`                             | Batch delay (tăng throughput) | 10–50ms                                         |
| `batch.size`                            | Batch buffer                  | 32 KB – 64 KB                                   |
| `buffer.memory`                         | Tổng bộ nhớ producer          | 32 MB – 256 MB                                  |
| `max.in.flight.requests.per.connection` | Message song song             | 1–5 (biz), 10 (log)                             |
| `retries`                               | Số lần retry                  | 3–5                                             |
| `delivery.timeout.ms`                   | Thời gian timeout             | 120000 (2 phút)                                 |

Cluster 1 – Log Producer (Fluentd/Filebeat/Agent)
```properties
acks=1
compression.type=snappy
linger.ms=50
batch.size=65536
buffer.memory=67108864
retries=2
max.in.flight.requests.per.connection=10
```
Cluster 2 – Business Producer (Spring)
```properties
acks=all
compression.type=lz4
linger.ms=10
batch.size=32768
buffer.memory=33554432
retries=5
max.in.flight.requests.per.connection=1
```
Cluster 3 – Analytics Producer
```properties
acks=1
compression.type=zstd
linger.ms=30
batch.size=65536
buffer.memory=134217728
```

---

### Consumer config (Spring Boot hoặc Kafka Streams)

| Config                      | Mô tả                                | Giá trị gợi ý                |
|-----------------------------|--------------------------------------|------------------------------|
| `fetch.min.bytes`           | Giảm round-trip                      | 1 KB – 50 KB                 |
| `fetch.max.wait.ms`         | Delay batch                          | 50 – 100ms                   |
| `max.poll.records`          | Số record mỗi poll                   | 500 – 1000 (biz), 5000 (log) |
| `enable.auto.commit`        | Commit offset tự động                | false (biz), true (log)      |
| `auto.offset.reset`         | Hành vi khi không có offset          | latest                       |
| `max.partition.fetch.bytes` | Dung lượng tối đa mỗi partition/poll | 1 MB – 5 MB                  |
| `session.timeout.ms`        | Thời gian heartbeat                  | 45s                          |

Cluster 1 – Log Consumer (batch sink)
```properties
enable.auto.commit=true
max.poll.records=5000
fetch.min.bytes=65536
fetch.max.wait.ms=100
auto.offset.reset=latest
```
Cluster 2 – Business Consumer (Spring Boot service)
```properties
enable.auto.commit=false
max.poll.records=500
fetch.min.bytes=1024
fetch.max.wait.ms=50
max.poll.interval.ms=300000
```
Cluster 3 – Analytics Consumer (ETL / Spark / Connect)
```properties
enable.auto.commit=true
fetch.min.bytes=131072
fetch.max.wait.ms=200
max.poll.records=10000
```

___
Spring Boot Kafka (application.yml) - business cluster
```yaml
spring:
  kafka:
    bootstrap-servers: kafka-biz:9092
    producer:
      acks: all
      compression-type: lz4
      batch-size: 32768
      buffer-memory: 33554432
      linger-ms: 10
      retries: 5
    consumer:
      enable-auto-commit: false
      group-id: serviceA-consumer
      max-poll-records: 500
      fetch-min-size: 1024
      fetch-max-wait: 50
```
