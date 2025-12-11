| Tab                    | Chức năng                                                     |
|------------------------|---------------------------------------------------------------|
| **Overview**           | Java version, PID, uptime, arguments, system properties       |
| **Monitor**            | CPU usage, heap size, classes, threads, GC activity (biểu đồ) |
| **Threads**            | Danh sách tất cả threads, trạng thái, deadlock detection      |
| **Sampler / Profiler** | Thống kê method nào tốn CPU / memory nhất                     |
| **Visual GC** (plugin) | Biểu đồ chi tiết về Eden / Survivor / Old Gen                 |
| **Heap Dump**          | Chụp snapshot memory → xem object nào chiếm heap              |
| **MBeans**             | Xem JMX metrics (Spring, HikariCP, etc.)                      |

| Mục tiêu        | Cách dùng VisualVM               |
|-----------------|----------------------------------|
| App chậm        | Sampler → CPU                    |
| RAM tăng dần    | Heap Dump → tìm object leak      |
| Thread treo     | Threads → xem thread state       |
| GC nhiều        | Visual GC → theo dõi GC activity |
| Connection leak | MBeans → xem HikariCP metrics    |

### Run your Java app with JMX enabled
Open firewall port 9010 (`ufw allow 9010`/tcp hoặc trong cloud security group).
```shell
  java \
  -Xms512m -Xmx1024m \
  -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9010 \
  -Dcom.sun.management.jmxremote.rmi.port=9010 \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Djava.rmi.server.hostname=192.168.1.100 \
  -jar my-spring-app.jar
```
`ssl=false` and `authenticate=false` just for dev/staging

### Docker with JMX
```dockerfile
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY target/my-spring-app.jar app.jar

# Mở port JMX (9010)
EXPOSE 8080 9010

ENTRYPOINT ["java",
    "-Xms512m", "-Xmx1024m",
    "-Dcom.sun.management.jmxremote",
    "-Dcom.sun.management.jmxremote.port=9010",
    "-Dcom.sun.management.jmxremote.rmi.port=9010",
    "-Dcom.sun.management.jmxremote.ssl=false",
    "-Dcom.sun.management.jmxremote.authenticate=false",
    "-Djava.rmi.server.hostname=0.0.0.0",
    "-jar", "app.jar"
]
```
`ssl=false` and `authenticate=false` just for dev/staging

### Docker Compose
```yaml
version: "3.8"

services:
  my-spring-app:
    build: .
    container_name: my-spring-app
    ports:
      - "8080:8080"    # HTTP port
      - "9010:9010"    # JMX port for VisualVM
    environment:
      - JMX_PORT=9010
      - SERVER_PORT=8080
      - JAVA_OPTS=-Xms512m -Xmx1024m

```


If you want more security
```dockerfile
    "-Dcom.sun.management.jmxremote.authenticate=true"
    "-Dcom.sun.management.jmxremote.password.file=/app/jmxremote.password"
    "-Dcom.sun.management.jmxremote.access.file=/app/jmxremote.access"
```
And file
```markdown
/opt/jmxremote.access
---------------------
monitorRole readonly
controlRole readwrite

/opt/jmxremote.password
---------------------
monitorRole mypass
controlRole mypass2
```
then set permission `chmod 600 /opt/jmxremote.*`

With spring
```yml
  management:
  endpoints:
    web:
      exposure:
        include: "health,info,metrics,threaddump,heapdump"
    jmx:
      exposure:
        include: "*"
```
- Help VisualVM (tab MBeans) can read metrics from Spring include:
`jvm.memory.used`
`system.cpu.usage`
`http.server.requests`
`hikaricp.connections.active`
