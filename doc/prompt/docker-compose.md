# Yêu cầu: Viết Docker Compose High-Performance Setup

**Vai trò:** Senior System Architect.

**Bối cảnh:**
Tôi cần file `docker-compose.yml` để chạy stack gồm:
* **Service A:** [Tên App, ví dụ: Java 17]
* **Service B:** [Database, ví dụ: Postgres 15]
* **Service C:** [Cache/Queue, ví dụ: Redis, Kafka]

**Yêu cầu kỹ thuật nâng cao (Constraints):**

1.  **Resource Limits (Mô phỏng K8s):**
  * Định nghĩa rõ `deploy.resources` cho TẤT CẢ services.
  * Thiết lập `limits` (trần) và `reservations` (sàn) cho CPU và Memory.
  * *Lưu ý:* Service App Java phải cấu hình sao cho khớp với các flag `-XX:MaxRAMPercentage` trong Dockerfile (Ví dụ: Limit 4G thì JVM nhận diện là 3G nếu set 75%).

2.  **Startup Order & Healthcheck (Sống sót):**
  * Viết `healthcheck` script chi tiết cho Database và Cache (test connection, không chỉ check ping).
  * Service App phải đợi (`depends_on` condition `service_healthy`) cho DB/Cache sẵn sàng 100% mới được start. Tránh crash loop khi khởi động.

3.  **Network Security:**
  * Tạo 2 networks: `frontend` (public) và `backend` (private) nếu các service có phần giống thế.
  * Database/Redis KHÔNG được expose port ra máy host (dùng `expose` thay vì `ports`), chỉ App mới kết nối được.

4.  **Kernel/System Tuning (Optional):**
  * Nếu cần chỉnh `ulimit` (số file mở) hoặc `sysctl` (swapiness, net core) cho Database/Kafka hoạt động tốt, hãy thêm vào file compose.

**Output:**
* File `docker-compose.yml`.
* File `.env` mẫu.
* Giải thích logic tính toán Resource Limit so với cấu hình App.