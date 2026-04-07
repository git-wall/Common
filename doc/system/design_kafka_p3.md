# Các chi tiết cấu hình Kafka

## Kafka Producer Tuning
| Parameter                               | Recommended Value                            | Description                                                         |
|-----------------------------------------|----------------------------------------------|---------------------------------------------------------------------|
| `acks`                                  | `all`                                        | Ensures all replicas acknowledge the message for durability.        |
| `compression.type`                      | `snappy` or `lz4` or 'zstd'                  | Balances compression speed and efficiency.                          |
| `batch.size`                            | `32768` (32 KB) or higher                    | Larger batches improve throughput.                                  |
| `linger.ms`                             | `5` to `20` ms                               | Allows batching of messages for better throughput.                  |
| `buffer.memory`                         | `64MB` or higher                             | Increases buffer size for high-throughput scenarios.                |
| `max.in.flight.requests.per.connection` | `5` or lower                                 | Limits in-flight requests to maintain message order.                |           
| `retries`                               | `3` or higher                                | Enables retries for transient failures.                             |
| `delivery.timeout.ms`                   | `120000` (2 minutes)                         | Sets maximum time to deliver a message.                             |
| `enable.idempotence`                    | `true`                                       | Ensures exactly-once delivery semantics.                            |
| `request.timeout.ms`                    | `30000` (30 seconds)                         | Sets timeout for broker requests.                                   |
| `max.block.ms`                          | `60000` (60 seconds)                         | Sets maximum time to block on producer send calls.                  |
| `client.id`                             | Unique identifier                            | Helps in tracking and logging producer activity.                    |
| `metrics.sample.window.ms`              | `10000` (10 seconds)                         | Sets the metrics sampling window.                                   |
| `metrics.num.samples`                   | `5`                                          | Number of samples to retain for metrics.                            |
| `security.protocol`                     | `SSL` or `SASL_SSL`                          | Secures communication with the Kafka broker.                        |
| `ssl.endpoint.identification.algorithm` | `https` or empty                             | Configures hostname verification for SSL connections.               |
| `sasl.mechanism`                        | `PLAIN`, `SCRAM-SHA-256`, or `SCRAM-SHA-512` | Specifies the SASL authentication mechanism.                        |
| `sasl.jaas.config`                      | JAAS configuration string                    | Provides SASL authentication details.                               |
| `transaction.timeout.ms`                | `60000` (60 seconds)                         | Sets timeout for transactions when using transactional producers.   |
| `transactional.id`                      | Unique identifier                            | Enables transactional messaging for exactly-once semantics.         |
| `max.request.size`                      | `1048576` (1 MB) or higher                   | Increases maximum request size for large messages.                  |
| `send.buffer.bytes`                     | `131072` (128 KB) or higher                  | Increases socket send buffer size for high-throughput scenarios.    |
| `receive.buffer.bytes`                  | `131072` (128 KB) or higher                  | Increases socket receive buffer size for high-throughput scenarios. |

Cấu hình chuẩn
```properties
acks=all // đảm bảo tất cả các replica xác nhận tin nhắn để đảm bảo độ bền

# lưu ý 
# điều chỉnh cao thì request ít hơn tránh bão request gây tắc nghẽn 
# nhưng đừng cao quá tránh làm giảm thông lượng (16384 hoặc 32768 là phổ biến)
# linger.ms nên được điều chỉnh (5 hoặc 10 ms) để cho phép gom nhóm tin nhắn
# kết hợp với nén dữ liệu để cải thiện tốc độ qua mạng với cấu hình batch này
batch.size=32768        // kích thước batch lớn hơn cải thiện thông lượng
linger.ms=10            // cho phép gom nhóm tin nhắn để cải thiện thông lượng
compression.type=snappy // cân bằng giữa tốc độ và hiệu quả nén khi qua mạng

# Xử lý Backpressure: 
# nếu broker chậm hơn producer sẽ làm đầy bộ đệm
# tăng kích thước bộ đệm để xử lý các kịch bản thông lượng cao
buffer.memory=67108864 // tăng kích thước bộ đệm cho các kịch bản thông lượng cao

max.in.flight.requests.per.connection=5 // giới hạn các yêu cầu đang chờ để duy trì thứ tự tin nhắn
retries=3 // cho phép thử lại trong trường hợp thất bại tạm thời không nên đặt quá cao tránh tràn bộ đệm
delivery.timeout.ms=120000 // đặt thời gian tối đa để giao tin nhắn
enable.idempotence=true // đảm bảo ngữ nghĩa giao hàng chính xác một lần
request.timeout.ms=30000 // đặt thời gian chờ cho các yêu cầu đến broker
max.block.ms=60000 // đặt thời gian tối đa để chặn các cuộc gọi gửi của producer
client.id=your-unique-client-id // giúp theo dõi và ghi nhật ký hoạt động của producer
metrics.sample.window.ms=10000 // đặt cửa sổ lấy mẫu số liệu
metrics.num.samples=5 // số mẫu để giữ lại cho số liệu
transaction.timeout.ms=60000 // đặt thời gian chờ cho các giao dịch khi sử dụng producer giao dịch
transactional.id=your-unique-transactional-id // cho phép nhắn tin giao dịch để có ngữ nghĩa chính xác một lần
max.request.size=1048576 // tăng kích thước yêu cầu tối đa cho các tin nhắn lớn
send.buffer.bytes=131072 // tăng kích thước bộ đệm gửi socket cho các kịch bản thông lượng cao
receive.buffer.bytes=131072 // tăng kích thước bộ đệm nhận socket cho các kịch bản thông lượng cao
```

Nếu cần thêm security
```properties
security.protocol=SASL_SSL
ssl.endpoint.identification.algorithm=https
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="your-username" password="your-password";
```
Lưu ý về `acks`:
- `acks=0`: Producer không chờ xác nhận từ broker. Tốc độ cao nhưng có nguy cơ mất dữ liệu.
- `acks=1`: Producer chờ xác nhận từ leader broker. Cân bằng giữa tốc độ và độ bền, nhưng vẫn có rủi ro mất dữ liệu nếu leader gặp sự cố trước khi follower kịp sao chép.
- `acks=all` (hoặc `acks=-1`): Producer chờ xác nhận từ tất cả các replica trong ISR (In-Sync Replicas). Cung cấp độ bền cao nhất, đảm bảo rằng dữ liệu không bị mất ngay cả khi một số broker gặp sự cố.

Với `acks=all` kết hợp `min.insync.replicas` >= 2 (giả sử `replication.factor` >= 3) đối với dữ liệu quan trọng để đạt được độ bền dữ liệu rất cao.
Độ bền dữ liệu:
- Quy tắc đơn giản số nhiều là được đảm bảo dữ liệu không bị mất.
- Và nhiều thì bắt đầu bằng 2 là đủ, nếu replication.factor >3 thì cân nhắc + 1 nữa.
- Ví dụ: Với `replication.factor=5` thì `min.insync.replicas=3` là một lựa chọn tốt.

## Kafka Consumer
| Parameter                               | Recommended Value                            | Description                                                         |
|-----------------------------------------|----------------------------------------------|---------------------------------------------------------------------|
| `group.id`                              | Unique consumer group ID                     | Identifies the consumer group for load balancing.                   |
| `enable.auto.commit`                    | `false`                                      | Disables auto-commit for better control over offsets.               |
| `auto.offset.reset`                     | `earliest` or `latest`                       | Determines where to start consuming if no offset is found.          |
| `fetch.min.bytes`                       | `1` or higher                                | Minimum bytes to fetch in a request.                                |
| `fetch.max.wait.ms`                     | `500` ms                                     | Maximum wait time for fetching data.                                |
| `max.partition.fetch.bytes`             | `1048576` (1 MB) or higher                   | Increases maximum fetch size per partition.                         |
| `session.timeout.ms`                    | `10000` (10 seconds)                         | Sets the timeout for consumer group session.                        |
| `heartbeat.interval.ms`                 | `3000` (3 seconds)                           | Interval for sending heartbeats to the broker.                      |
| `max.poll.records`                      | `500` or lower                               | Limits the number of records returned in a single poll.             |
| `max.poll.interval.ms`                  | `300000` (5 minutes)                         | Maximum delay between polls before the consumer is considered dead. |
| `client.id`                             | Unique identifier                            | Helps in tracking and logging consumer activity.                    |
| `isolation.level`                       | `read_committed`                             | Ensures only committed messages are read in transactional topics.   |
| `security.protocol`                     | `SSL` or `SASL_SSL`                          | Secures communication with the Kafka broker.                        |
| `ssl.endpoint.identification.algorithm` | `https` or empty                             | Configures hostname verification for SSL connections.               |
| `sasl.mechanism`                        | `PLAIN`, `SCRAM-SHA-256`, or `SCRAM-SHA-512` | Specifies the SASL authentication mechanism.                        |
| `sasl.jaas.config`                      | JAAS configuration string                    | Provides SASL authentication details.                               |

Cấu hình chuẩn
```properties
group.id=your-unique-consumer-group-id // xác định nhóm consumer để cân bằng tải

# tắt tự động cam kết để kiểm soát tốt hơn các offset
# false trong case business, true trong case log
enable.auto.commit=false

auto.offset.reset=earliest // xác định nơi bắt đầu tiêu thụ nếu không tìm thấy offset

# số byte tối thiểu để lấy trong một yêu cầu
# 1 (1byte) - 1024 (1KB) Độ trễ thấp (low latency) nhưng tốn nhiều request nhỏ (tốn CPU/network)
# 1048576 (1MB) hoặc hơn Tối ưu throughput, có thể tăng độ trễ đáng kể nếu dữ liệu không đủ.
fetch.min.bytes=1
# Real-time: 10-20 MB để giảm độ trễ và tăng tốc độ phản hồi
# Log/ETL: 50 MB hoặc cao hơn để tối ưu thông lượng
fetch.max.bytes=52428800 // 50 MB
# giới hạn số bản ghi trả về trong một lần poll
# 100-500 đối với business và 1000 đối với log để cân bằng tải và thông lượng
# có thể sẽ thấp hơn nếu xử lý mỗi bản ghi mất nhiều thời gian tránh bị
# kích khỏi group gây bão re-balance
max.poll.records=500

# thời gian chờ tối đa để lấy dữ liệu
# 50 ms đối business và 1000 ms đối log để delay batch và cải thiện thông lượng
fetch.max.wait.ms=500 // thời gian chờ tối đa để lấy dữ liệu

# tăng kích thước lấy tối đa cho mỗi phân vùng
max.partition.fetch.bytes=1048576 // 1 MB hoặc cao hơn để lấy các tin nhắn lớn hơn

session.timeout.ms=10000 // đặt thời gian chờ cho phiên nhóm consumer
heartbeat.interval.ms=3000 // khoảng thời gian gửi tín hiệu sống đến broker
max.poll.interval.ms=300000 // độ trễ tối đa giữa các lần poll trước khi consumer bị coi là chết
client.id=your-unique-client-id // giúp theo dõi và ghi nhật ký hoạt động của consumer
isolation.level=read_committed // đảm bảo chỉ đọc các tin nhắn đã cam kết trong các chủ đề giao dịch
```

Nếu cần thêm security
```properties
security.protocol=SASL_SSL
ssl.endpoint.identification.algorithm=https
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="your-username" password="your-password";
```

`auto.offset.reset`: Quyết định cách Consumer xử lý khi một consumer group mới tham gia, hoặc khi offset hiện tại không còn hợp lệ (ví dụ, đã bị xóa do chính sách lưu trữ).

- latest (mặc định): Bắt đầu tiêu thụ từ những tin nhắn mới nhất trong partition. Các tin nhắn cũ hơn sẽ được bỏ qua.
- earliest: Bắt đầu tiêu thụ từ đầu partition, xử lý lại toàn bộ dữ liệu có sẵn. Anh em cần cẩn thận vì có thể phải xử lý lại một lượng lớn dữ liệu.
- none: Nếu không tìm thấy offset hợp lệ, Consumer sẽ ném ra một exception. Cách này cho phép anh em kiểm soát tối đa việc xử lý lỗi nhưng cũng tăng thêm độ phức tạp.
- Việc chọn auto.offset.reset giống như quyết định đọc một bộ truyện từ đâu sau khi làm mất dấu trang. latest là bỏ qua các tập cũ và đọc ngay tập mới nhất. earliest là đọc lại từ tập đầu tiên. Còn none thì giống như việc người thủ thư sẽ yêu cầu anh em tự tìm lại đúng vị trí đã đánh dấu trước đó.

Consumer hoạt động theo các nhóm (group). <br>
Kafka sẽ tự động phân chia các partition của một topic cho các consumer trong cùng một nhóm. <br>
Mỗi partition chỉ được gán cho một consumer duy nhất trong nhóm đó để xử lý. <br>
Đây là cách Kafka đạt được khả năng xử lý song song và cân bằng tải ở phía consumer.

- **Rebalancing** (Tái cân bằng): Nếu một consumer tham gia hoặc rời khỏi nhóm (hoặc gặp sự cố), Kafka sẽ tự động phân chia lại các partition cho các consumer còn lại. Quá trình này gọi là rebalance. Nó cần thiết cho tính linh hoạt và khả năng co giãn, nhưng có thể tạm dừng việc xử lý dữ liệu trong một thời gian ngắn.
- Chiến lược phân chia Partition (partition.assignment.strategy):
  + **RangeAssignor** (mặc định): Phân chia các partition theo từng topic. Có thể dẫn đến tải không đồng đều nếu số lượng partition không chia hết cho số lượng consumer.
  + **RoundRobinAssignor**: Phân bổ các partition một cách tuần tự cho tất cả các consumer. Thường dẫn đến sự phân bổ đồng đều hơn.
  + **StickyAssignor**: Cố gắng duy trì các phân công partition hiện có trong quá trình rebalance để tránh những thay đổi không cần thiết. Điều này tốt cho các consumer có trạng thái (stateful) hoặc để giảm thiểu chi phí của việc rebalance.
  + **CooperativeStickyAssignor**: Một cải tiến mới cho phép rebalance diễn ra mượt mà hơn, giảm thiểu tình trạng "dừng cả thế giới" (stop-the-world). Consumer chỉ giải phóng các partition mà chúng cần giải phóng, trong khi các consumer khác vẫn tiếp tục xử lý các partition được giao.

Lưu ý 1: 
- Consumer lag không chỉ là một con số; nó là một triệu chứng của vấn đề tiềm ẩn. <br>
- Nguyên nhân có thể do consumer xử lý chậm (logic phức tạp), số lượng consumer không đủ, cấu hình fetching không tối ưu, hoặc thậm chí là vấn đề từ phía broker. <br>
- Tối ưu consumer thường là một cuộc chiến liên tục để giảm thiểu lag. Nhiều thiết lập của consumer (Workspace.min.bytes, max.poll.records, số lượng instance consumer so với số partition) chính là những công cụ để anh em kiểm soát tình trạng này. <br>

Lưu ý 2: 
- nếu `max.poll.records` quá cao và việc xử lý tin nhắn chậm, `max.poll.interval.ms` (thời gian tối đa một consumer phải gọi lại poll() trước khi bị coi là không hoạt động) có thể bị vượt quá. <br>
- Điều này khiến consumer bị loại khỏi nhóm, gây ra rebalance, tạm thời dừng việc tiêu thụ tin nhắn đối với các partition bị ảnh hưởng, và có khả năng làm tăng consumer lag. <br>
- Do đó, việc tinh chỉnh consumer đòi hỏi sự hiểu biết về mối tương quan giữa các tham số này, chứ không chỉ điều chỉnh các tham số riêng lẻ. <br>
- Chuyển việc xử lý nặng sang một luồng (thread) riêng biệt là một chiến lược thông minh để tách biệt việc polling dữ liệu khỏi các tác vụ xử lý tốn thời gian.

| Tham Số                           | Chức Năng                                                              | Mẹo Tối Ưu / Khi Nào Dùng                                                                                                  | Lưu Ý Tiềm Ẩn                                                                                    |
|-----------------------------------|------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| **Workspace.min.bytes**           | Lượng dữ liệu tối thiểu broker trả về cho mỗi yêu cầu fetch.           | Tăng để giảm số request, cải thiện thông lượng. Giảm để giảm độ trễ.                                                       | Tăng quá cao có thể gây trễ nếu dữ liệu đến không thường xuyên.                                  |
| **Workspace.max.wait.ms**         | Thời gian tối đa broker chờ để có đủ Workspace.min.bytes dữ liệu.      | Tăng để cho broker thêm thời gian gom đủ Workspace.min.bytes. Giảm để phản hồi nhanh hơn.                                  | Tăng quá cao làm tăng độ trễ tối đa.                                                             |
| **max.poll.records**              | Số lượng record tối đa trả về sau mỗi lần gọi poll().                  | Tăng để xử lý nhiều hơn mỗi lần poll, có thể tăng thông lượng. Giảm để xử lý nhanh hơn mỗi batch, tránh timeout.           | Tăng quá cao có thể gây OutOfMemoryError hoặc timeout `max.poll.interval.ms`, dẫn đến rebalance. |
| **enable.auto.commit**            | `true`: Kafka tự động commit offset. `false`: Commit offset thủ công.  | Đặt `false` và commit thủ công sau khi xử lý để đảm bảo “ít nhất một lần” hoặc “chính xác một lần” (với logic idempotent). | Đặt `true` có thể gây mất/lặp tin nhắn nếu consumer gặp sự cố trước khi commit.                  |
| **auto.offset.reset**             | Hành vi khi không có offset hợp lệ: `latest`, `earliest`, hoặc `none`. | `latest` cho dữ liệu mới. `earliest` để xử lý lại toàn bộ. `none` khi muốn kiểm soát hoàn toàn.                            | `earliest` có thể xử lý lại lượng lớn dữ liệu. `latest` bỏ qua dữ liệu cũ.                       |
| **partition.assignment.strategy** | Cách Kafka phân chia partition cho consumer.                           | `Sticky` hoặc `CooperativeSticky` để giảm thiểu xáo trộn khi rebalance. `RoundRobin` cho phân phối đồng đều hơn.           | `Range` có thể không đều nếu số partition không chia hết cho số consumer.                        |


## Kafka Broker Tuning
| Parameter                        | Recommended Value                | Description                                                      |
|----------------------------------|----------------------------------|------------------------------------------------------------------|
| `num.network.threads`            | `3` or higher (Tùy core CPU)     | Increases network threads for high-throughput scenarios.         |
| `num.io.threads`                 | `8` or higher (Tùy dick và core) | Increases I/O threads for better disk performance.               |
| `socket.send.buffer.bytes`       | `131072` (128 KB) or higher      | Increases socket send buffer size.                               |
| `socket.receive.buffer.bytes`    | `131072` (128 KB) or higher      | Increases socket receive buffer size.                            |
| `socket.request.max.bytes`       | `104857600` (100 MB) or higher   | Increases maximum request size.                                  |
| `log.segment.bytes`              | `1073741824` (1 GB) or higher    | Increases log segment size for better disk utilization.          |
| `log.retention.hours`            | `168` (7 days) or higher         | Sets log retention period.                                       |
| `log.retention.bytes`            | `-1` (unlimited) or higher       | Sets log retention size limit.                                   |
| `log.cleaner.enable`             | `true`                           | Enables log compaction for topics that require it.               |
| `log.cleaner.threads`            | `2` or higher                    | Increases log cleaner threads for better compaction performance. |
| `log.cleaner.dedupe.buffer.size` | `134217728` (128 MB) or higher   | Increases buffer size for log cleaner deduplication.             |
| `message.max.bytes`              | `10485760` (10 MB) or higher     | Increases maximum message size.                                  |
| `replica.fetch.min.bytes`        | Business: 1-8 KB Log/ETL: 1MB    | Increases maximum fetch size for replicas.                       |
| `replica.fetch.max.bytes`        | `1048576` (1 MB) or higher       | Increases maximum fetch size for replicas.                       |
| `replica.fetch.wait.max.ms`      | `500` ms                         | Sets maximum wait time for replica fetch requests.               |
| `unclean.leader.election.enable` | `false`                          | Prevents data loss during leader elections.                      |
| `workspace.min.bytes`            | `1048576` (1 MB) or higher       | Increases minimum bytes for workspace.                           |
| `workspace.max.waits.ms`         | `3` or higher                    | Increases maximum waits for workspace.                           |
| `min.insync.replicas`            | `2` or higher                    | Ensures a minimum number of in-sync replicas for durability.     |

| Khu Vực                    | Tham Số Chính (ví dụ)                                                       | Mẹo Nhanh / Tác Động                                                                                                 | "Đừng Quên!" (ví dụ)                                                   |
|----------------------------|-----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------|
| **Luồng (Threads)**        | `num.network.threads`, `num.io.threads`                                     | Tăng dựa trên số lõi CPU và tốc độ đĩa.                                                                              | Theo dõi CPU utilization để tránh context switching quá nhiều.         |
| **Bộ nhớ (Memory)**        | `JVM Heap Size`, `OS Page Cache`                                            | Tối ưu JVM Heap để giảm GC pauses. Cung cấp nhiều RAM cho OS Page Cache để tăng tốc I/O.                             | Kafka tận dụng rất nhiều OS Page Cache.                                |
| **Đĩa (Disk)**             | `log.segment.bytes`, `log.dirs`, *Loại đĩa*                                 | Sử dụng SSD. Phân tán `log.dirs` trên nhiều đĩa. Cân nhắc `log.segment.bytes` cho việc dọn dẹp và quản lý file.      | SSD là yếu tố thay đổi cuộc chơi cho Kafka I/O.                        |
| **Mạng (Network)**         | `socket.send.buffer.bytes`, `socket.receive.buffer.bytes`                   | Tăng cho mạng có độ trễ cao / thông lượng cao. Cần phối hợp với cấu hình ở OS.                                       | Phối hợp với quản trị viên hệ thống để tối ưu TCP stack của OS.        |
| **Sao chép (Replication)** | `default.replication.factor`, `min.insync.replicas`, `num.replica.fetchers` | `replication.factor=3`, `min.insync.replicas=2` là cấu hình phổ biến. Tăng `num.replica.fetchers` nếu sao chép chậm. | Cân bằng giữa độ bền (*durability*) và tính sẵn sàng (*availability*). |

**Replication Factor**: 
- Đảm bảo độ bền và tính sẵn sàng của dữ liệu <br>
- Replication factor xác định số lượng bản sao chính xác của mỗi partition được lưu trữ trên các broker khác nhau. <br>
- Nếu một broker chứa leader của partition gặp sự cố, một follower có thể được bầu chọn để thay thế. Đây là cơ chế quan trọng đảm bảo độ bền và tính sẵn sàng cho dữ liệu của anh em.

- Cấu hình phổ biến: Replication factor bằng 3 rất được ưa chuộng cho môi trường production: một leader, hai follower, được phân bổ trên các broker khác nhau (lý tưởng là ở các rack hoặc availability zone (AZ) khác nhau).
- Đánh đổi: Replication factor cao hơn đồng nghĩa với khả năng chịu lỗi tốt hơn nhưng cũng tiêu tốn nhiều không gian đĩa hơn và tạo ra nhiều lưu lượng mạng hơn cho việc sao chép giữa các broker.
- Kết hợp với `min.insync.replicas`: Hai tham số này phối hợp với nhau để quyết định mức độ đảm bảo độ bền thực tế cho dữ liệu của anh em.

# Tip
## Consumer
`fetch.min.bytes ≈ broker.fetch.min.bytes` <br>
`max.partition.fetch.bytes ≤ broker.fetch.max.bytes`

## Triết lý xử lý
- Consumer: nhận, xác thực, ủy thác -> đẩy sang bước khác tránh xử lý nặng
- 1 Topic - 1 Loại Công Việc không all in one topic
- Xử lý song song: nhiều consumer trong cùng group
- Xử lý bất đồng bộ: sử dụng thread pool trong consumer để xử lý công việc
- Giới hạn số bản ghi poll: `max.poll.records` để tránh xử lý quá lâu bị kick khỏi group
- Xử lý idempotent: tránh duplicate khi retry
- Sử dụng transaction nếu cần thiết
- Quản lý offset thủ công: tắt `enable.auto.commit` để kiểm soát offset tốt hơn
- Sử dụng backoff retry và DLQ để xử lý lỗi

## Phân vùng
Vùng "Goldilocks"
- Không Quá Ít, Cũng Không Quá Nhiều Partition. 
- Chọn số partition rất quan trọng. 
- Quá ít, hạn chế xử lý song song của consumer. 
- Quá nhiều, tăng gánh nặng cho broker (quản lý metadata, file handle) và có thể làm chậm bầu chọn leader.

Chiến Lược Phân Vùng
- Dựa Trên Key: Đảm bảo thứ tự cho các tin nhắn cùng key.
- Round-Robin: Tối ưu hóa phân phối tải nhưng không đảm bảo thứ tự.

## Retry và DLQ
để tránh dính pattern poison pill
- retry topic
- DLQ - (Dead Letter Queue) dead letter topic

setup backoff retry với exponential backoff
- `initial interval: 100ms`
- `multiplier: 2.0`
- `max interval: 10s`
- `max attempts: 5`

điều này được cung cấp bởi spring-kafka hoặc kafka-streams

# Monitor
Theo dõi các chỉ số:
* kafka.network:type=RequestMetrics,name=RequestsPerSec,request=FetchConsumer
* kafka.network:type=RequestMetrics,name=TotalTimeMs,request=FetchConsumer
* kafka.server:type=ReplicaFetcherManager,name=MaxLag,clientId=Replica <br>
_**→ Để thấy consumer fetch chậm do network hay broker delay**_

Tăng buffer nếu network ổn định

`socket.receive.buffer.bytes=1048576`
`socket.send.buffer.bytes=1048576`


