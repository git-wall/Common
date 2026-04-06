# Yêu cầu: Viết Dockerfile Production-Grade (Deep Optimization)

**Vai trò:** Bạn là Principal DevOps Engineer & Language Expert (Java/Node/Go...).

**Input Context (Ngữ cảnh đầu vào):**
* **Ngôn ngữ/Framework:** [ĐIỀN VÀO ĐÂY: Ví dụ Java 17 Spring Boot, Golang 1.21 Gin, NodeJS 18 NestJS...]
* **Mẫu tham chiếu (Reference Standard):** Tôi có một mẫu Dockerfile chuẩn mực dưới đây. Hãy đọc kỹ nó để hiểu mức độ tối ưu tôi mong muốn (về size, memory tuning, GC, security).
    ```dockerfile
    [COPY PASTE ĐOẠN CODE DOCKERFILE CỦA BẠN VÀO ĐÂY]
    ```

**Nhiệm vụ:**
Viết một Dockerfile mới cho dự án hiện tại của tôi, nhưng phải áp dụng các kỹ thuật tối ưu **tương đương hoặc tốt hơn** mẫu tham chiếu trên, phù hợp với ngôn ngữ tôi yêu cầu.

**Checklist bắt buộc (Constraints):**

1.  **Build Optimization (Size & Layers):**
  * Sử dụng Multi-stage build triệt để.
  * **Với Java:** Nếu ngữ cảnh tôi cung cấp cho phép thì dùng `jdeps` và `jlink` để tạo Custom JRE (nếu version hỗ trợ) có thể giống mẫu tham chiếu nếu có.
  * **Với Node/Go/Rust:** Phải tách tầng build dependencies và tầng runtime, xóa cache, strip binary (với Go).
  * Sắp xếp layer để tối ưu Docker Cache (Copy dependency file trước -> install -> copy source).

2.  **Runtime Performance (Flags & Tuning):**
  * Cấu hình `ENTRYPOINT` với các cờ tối ưu nhất cho Container.
  * Xử lý Memory Limit: Phải có flag tự động nhận diện RAM container (Ví dụ: `-XX:MaxRAMPercentage` cho Java, hoặc set memory limit cho Node v8).
  * Garbage Collection (GC): Gợi ý GC phù hợp nhất cho container (ZGC, G1GC, v.v...) và giải thích lý do chọn.
  * NUMA Awareness: Nếu ngôn ngữ hỗ trợ (như Java), hãy bật nó.

3.  **Security Hardening:**
  * **Non-root User:** Tuyệt đối không chạy bằng root. Tạo user riêng (UID 1000) và `chown` quyền hạn chế nhất có thể.
  * **Base Image:** Sử dụng `distroless` hoặc `alpine/slim` phiên bản mới nhất, quét lỗi bảo mật (giả định).

4.  **Stability:**
  * Xử lý tín hiệu hệ thống (SIGTERM/SIGINT) để Graceful Shutdown.

**Output mong đợi:**
1.  Full Dockerfile code.
2.  Bảng giải thích chi tiết các flag sử dụng trong `ENTRYPOINT` (Tại sao dùng flag này? Tác dụng gì với Memory/CPU?).
3.  So sánh ước lượng size image trước và sau khi tối ưu.