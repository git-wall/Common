# Định dạng dữ liệu 
Chọn Serialization  (Serializer/Deserializer) phù hợp để định dạng dữ liệu trong Kafka. 
Các định dạng phổ biến bao gồm 
- `JSON` (Phổ biến dễ đọc nhưng kích thước lớn và hiệu suất thấp)
- `Avro` 
  - Định dạng nhị phân nhỏ gọn (nhỏ hơn JSON tới 30-40%). 
  - Tốc độ (De)serialize nhanh (nhanh hơn JSON khoảng 20%). 
  - Hỗ trợ tiến hóa schema rất mạnh mẽ (schema được đi kèm với dữ liệu hoặc qua Schema Registry, cho phép tương thích tiến/lùi mà không gây lỗi cho consumer). 
  - Lý tưởng cho xử lý batch do lưu trữ hiệu quả cao.
- `Protobuf` 
  - Định dạng nhị phân cực kỳ nhỏ gọn. 
  - Tốc độ (De)serialize rất nhanh (có thể nhanh hơn JSON 30-50%). 
  - Tiến hóa schema mạnh mẽ (sử dụng số thứ tự của trường). 
  - Đa ngôn ngữ (tương thích với nhiều ngôn ngữ lập trình).
- `MessagePack` (Nhỏ gọn, hiệu suất tốt)
- `Thrift` (Hiệu suất tốt, hỗ trợ schema evolution)

Mỗi định dạng có ưu và nhược điểm riêng về hiệu suất, khả năng mở rộng và tính tương thích ngược.

| Format                       | Serialize (µs) | Deserialize (µs) | Size (bytes) |
|------------------------------|----------------|------------------|--------------|
| **Protobuf**                 | ~3.5           | ~4.1             | ~540         |
| **Thrift (binary protocol)** | ~3.8           | ~4.4             | ~560         |
| **MessagePack**              | ~4.2           | ~4.8             | ~680         |
| **JSON (Jackson)**           | ~18            | ~20              | ~1200        |

về mọi mặt (hiệu suất, kích thước, khả năng tiến hóa schema), 
Protobuf thường là lựa chọn tốt nhất cho các hệ thống yêu cầu hiệu suất cao và khả năng mở rộng.