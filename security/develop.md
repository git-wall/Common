### Develop:
- **_`Keycloak`_** làm "Source of Truth" cho User: Chỉ dùng để xác thực và lấy thông tin User cơ bản.
- **_`Casbin`_** để quản lý Permission nghiệp vụ: * Lưu Policy trong Database (MySQL/PostgreSQL) thông qua Adapter.
 Dùng **_`Redis`_** Watcher để khi bạn update quyền ở node A, các node B, C... tự động reload policy vào bộ nhớ.
- chuyển **_`OPA`_** khi: Bạn có quá nhiều service bằng các ngôn ngữ khác nhau (Go, Java, Nodejs, Python) 
và muốn có một bộ quy tắc (Policy) dùng chung duy nhất cho tất cả, hoặc khi cần áp dụng chính sách cho cả tầng hạ tầng (Kubernetes).

### Extend:
Nếu hệ thống chưa đến mức "khổng lồ": 
*   Hãy dừng lại ở Casdoor + Casbin. Casdoor cung cấp UI quản lý cực tốt cho Casbin. 
*   Casbin chạy trong Java cực nhanh, dùng Redis để sync policy giữa các node là quá đủ cho các bài toán High Concurrency.

Nếu bắt buộc phải dùng OPA (do yêu cầu compliance hoặc đa ngôn ngữ): 
* Dùng **_`Casdoor`_** để quản lý Identity (Người dùng, Nhóm).
* Dùng _**`OPAL`**_ để theo dõi sự thay đổi trong Database của Casdoor và tự động cập nhật vào OPA.
