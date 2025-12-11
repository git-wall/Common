# Mindset “domain-based cluster design”
## Kafka cluster

Tư duy chia theo mục đích (hybrid):
* Mở rộng dễ (scale từng cluster riêng).
* Bảo trì, upgrade, migration không chạm domain khác.
* Gắn chính sách bảo mật và quota khác nhau cho từng nhóm.

___
### Cluster Log
- Mục đích: Lưu trữ log hệ thống, log ứng dụng, log truy cập...
- Đặc điểm:
  + Throughput cao (TB/ngày)
  + Dung lượng lớn (PB)
  + Latency thấp không quan trọng
  + Dữ liệu có thể tái tạo lại (replayable data)
  + Retention dài (7-30 ngày hoặc hơn)
- Ví dụ: Hệ thống thu thập log tập trung như ELK stack, Loki, Fluentd, Splunk...
- Gợi ý cấu hình:

| Thành phần             | Gợi ý cấu hình                                  |
|------------------------|-------------------------------------------------|
| **Replication Factor** | 2 (vì log có thể tái tạo lại, không cần RF=3)   |
| **Retention**          | 7–30 ngày                                       |
| **Compression**        | snappy hoặc lz4                                 |
| **Disk**               | HDD đủ, IO vừa                                  |
| **Throughput**         | TB/ngày là chuyện bình thường                   |
| **Consumer**           | Elasticsearch, Loki, S3, BigQuery...            |
| **Note**               | Tune `log.segment.bytes` lớn để giảm file count |

→ Cluster này chịu tải nặng nhất về dung lượng, nhưng ít ràng buộc latency.
___
### Cluster Business/Event Bus
Mục đích: giao tiếp giữa microservices, publish-subscribe, event sourcing.
Đặc điểm: latency thấp, độ tin cậy cao, kích thước message nhỏ hơn.

- Gợi ý cấu hình:

| Thành phần             | Gợi ý cấu hình                                                  |
|------------------------|-----------------------------------------------------------------|
| **Replication Factor** | 3 (bắt buộc để HA)                                              |
| **Retention**          | 1–7 ngày (tùy use case)                                         |
| **Compression**        | lz4 (tốc độ tốt, CPU nhẹ)                                       |
| **Disk**               | SSD nếu cần latency thấp                                        |
| **Throughput**         | vài GB–TB/ngày                                                  |
| **Consumer**           | service khác, Kafka Streams, Flink, ...                         |
| **Note**               | Có thể dùng Schema Registry (Avro/Protobuf) để kiểm soát schema |

→ Cluster này nên tách riêng để đảm bảo tốc độ, không bị ảnh hưởng bởi log-heavy traffic.
___
### Cluster Analytics / Data Lake Feed
Mục đích: tập trung dữ liệu từ các domain khác, đẩy sang hệ thống phân tích.
Đặc điểm: batch-oriented, throughput lớn, không cần latency thấp.

- Gợi ý cấu hình:

| Thành phần             | Gợi ý cấu hình                                               |
|------------------------|--------------------------------------------------------------|
| **Replication Factor** | 2 (nếu có backup định kỳ) hoặc 3                             |
| **Retention**          | 1–3 ngày (vì dữ liệu được ETL ra hệ thống khác)              |
| **Compression**        | zstd hoặc lz4                                                |
| **Disk**               | HDD dung lượng cao                                           |
| **Consumer**           | Spark, Flink, Kafka Connect, Debezium, ...                   |
| **Note**               | Dùng MirrorMaker từ các cluster khác để sync topic cần thiết |

---
### Final

Cách kết nối các cluster lại (Bridge Layer)
- Dùng MirrorMaker 2 (MM2) để đồng bộ topic cần thiết, ví dụ:
  + business → analytics
  + log → analytics
- Dùng Schema Registry Global để đảm bảo schema consistency.
- Dùng Kafka Connect riêng cho ingest/export (connectors cho DB, S3, ClickHouse...).
- Dùng Prometheus + Grafana + Alertmanager để giám sát cluster nào “nóng”.