## Test
Unit Test (Kiểm thử đơn vị)
*    Mục đích: Kiểm thử từng đơn vị nhỏ (hàm, class) một cách độc lập.
*    Công cụ / Thư viện phổ biến:

    JUnit 5 – Framework test phổ biến nhất.
    
    TestNG – Một lựa chọn khác thay thế JUnit, mạnh hơn trong quản lý test case.
    
    Mockito – Mock đối tượng để kiểm thử độc lập.
    
    PowerMock – Mở rộng Mockito, dùng để mock static method, private method.
    
    AssertJ – Thư viện giúp viết assertion dễ đọc và linh hoạt.
    
    Hamcrest – Hỗ trợ viết matcher để so sánh giá trị.
    
    EasyMock – Một lựa chọn khác để mock đối tượng.

Integration Test (Kiểm thử tích hợp)
*    Mục đích: Kiểm tra tương tác giữa các module trong ứng dụng.
*    Công cụ / Thư viện phổ biến:

    Spring Boot Test (spring-boot-starter-test) – Hỗ trợ test toàn bộ ứng dụng Spring Boot.
    
    Testcontainers – Tạo môi trường giả lập (Docker-based) để test database, Kafka, Redis...
    
    WireMock – Giả lập API bên ngoài để test hệ thống.
    
    RestAssured – Kiểm thử REST API một cách dễ dàng.
    
    MockServer – Giả lập backend API server để kiểm thử.
    
    Awaitility – Hỗ trợ kiểm thử các tác vụ bất đồng bộ (async).

System Test (Kiểm thử hệ thống)
*    Mục đích: Kiểm thử toàn bộ hệ thống theo yêu cầu nghiệp vụ.
*    Công cụ / Thư viện phổ biến:

    Selenium – Kiểm thử giao diện UI (web).
    
    Cypress – Công cụ kiểm thử UI hiện đại, nhanh hơn Selenium.
    
    Appium – Kiểm thử ứng dụng di động (Android/iOS).
    
    Karate – Hỗ trợ kiểm thử API + UI tự động.
    
    Cucumber – Kiểm thử dựa trên BDD (Behavior-Driven Development).
    
    Postman/Newman – Tạo và chạy test API tự động.
    
    Gatling – Kiểm thử tải hệ thống kết hợp với test chức năng.

Performance Test (Kiểm thử hiệu năng)
*    Mục đích: Đánh giá hiệu suất của hệ thống khi xử lý tải lớn.
*    Công cụ / Thư viện phổ biến:

    JMeter – Công cụ kiểm thử tải phổ biến nhất.
    
    Gatling – Hỗ trợ kiểm thử hiệu năng với Scala.
    
    k6 – Công cụ test hiệu năng hiện đại, dễ sử dụng.
    
    Locust – Kiểm thử tải dựa trên Python.
    
    Jaeger – Hỗ trợ tracing hiệu năng trong hệ thống phân tán.
    
    JMH (Java Microbenchmark Harness) – Đo hiệu suất của từng phương thức Java.
    
    VisualVM – Giám sát và tối ưu hiệu suất ứng dụng Java.

Memory Test (Kiểm thử bộ nhớ)
* Mục đích: 

1. [ ] Kiểm tra kích thước đối tượng trên JVM.
2. [ ] Xác định cách sắp xếp bộ nhớ (Memory Layout) của các lớp.
3. [ ] Đánh giá mức độ tối ưu hóa bộ nhớ của đối tượng trong Java.

* Công cụ / Thư viện phổ biến:


    JOL (Java Object Layout) - Đo hiệu năng trong Java, phân tích bộ nhớ và đánh giá cách JVM quản lý bộ nhớ đối tượng.
    
    Eclipse MAT (Memory Analyzer Tool) - Phân tích heap dump để tìm memory leak.
        
    YourKit Profiler - Phân tích hiệu suất & bộ nhớ ứng dụng Java.
       
    GCEasy - Kiểm tra và tối ưu hóa Garbage Collection.


Security Test (Kiểm thử bảo mật)
Mục đích: Phát hiện lỗ hổng bảo mật trong ứng dụng.
Công cụ / Thư viện phổ biến:

    OWASP ZAP – Công cụ scan lỗ hổng bảo mật.
    
    Burp Suite – Kiểm tra bảo mật API, Web.
    
    SonarQube – Phân tích mã nguồn và tìm lỗ hổng bảo mật.
    
    Snyk – Kiểm tra bảo mật thư viện dependency.

Chaos Engineering (Kiểm thử độ ổn định)
*    Mục đích: Kiểm tra khả năng chịu lỗi của hệ thống trong điều kiện bất thường.
*    Công cụ / Thư viện phổ biến:

    Chaos Monkey – Tạo lỗi ngẫu nhiên để kiểm tra độ bền hệ thống.
    
    Gremlin – Công cụ phá hoại có kiểm soát để test hệ thống phân tán.
    
    Toxiproxy – Giả lập lỗi mạng để test ứng dụng.

### Spring boot option

JUnit 5 + Mockito (Unit Test).

    Spring Boot Test + Testcontainers (Integration Test)
    
    RestAssured / WireMock (API Test)
    
    JMeter / Gatling / JMH (Performance Test)
    
    OWASP ZAP / SonarQube (Security Test)