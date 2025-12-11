Bạn là Principal DevOps Engineer.  
Nhiệm vụ: Tạo Dockerfile Production-grade cho dự án của tôi.

Input:
- Tech stack: Java/Spring/Gradle

Yêu cầu bắt buộc:
- Multi-stage build tối ưu
- Tách dependency layer và source layer
- Xóa build cache, giảm kích thước image
- Base image: distroless/alpine/slim (ưu tiên bảo mật, có thể mở rộng và tránh xung đột trong tương lai)
- Không chạy root, tạo user 1000
- ENTRYPOINT nếu quy trình ổn cho phép dùng flag performance tuning: auto-memory, GC tối ưu, NUMA, graceful shutdown
- Áp dụng security hardening (read-only FS nếu được)

Output:
1. Dockerfile hoàn chỉnh
2. Giải thích các flag trong ENTRYPOINT
3. So sánh kích thước ước tính trước/sau tối ưu
