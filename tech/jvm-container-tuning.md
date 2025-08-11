# JVM Container Resource Tuning Guide

## 📌 Introduction

When deploying a Java application inside a container (e.g. Docker or Kubernetes), **you should not allocate 100% of system CPU or RAM to the JVM**, even if it's the only service running in the container.

---

## 📐 Thread Calculation (with Realistic CPU Adjustment)

> **Ideal Thread Count Formula** (theoretical):
```
Threads = CPU Cores × (1 + Wait Time / Compute Time)
```

However, in practice:

- JVM **should not use 100% of CPU** (to avoid GC starvation, native thread contention, and system instability).
- You must adjust the **CPU cores available to JVM** depending on the use case:

| Application Type     | Recommended CPU Usage (%) | Comment                                  |
|----------------------|---------------------------|------------------------------------------|
| Web/API (Spring Boot)| 50–70%                    | Leave headroom for GC, JIT, OS threads   |
| Batch/ETL            | 70–90%                    | Can consume more CPU for throughput      |
| Realtime/Low-latency | 40–60%                    | Requires minimal GC/JIT interference     |

### ✅ Practical Formula:

```
Effective Threads = (CPU Cores × Usage %) / CPU Usage per Thread
```

Where:
- `CPU Cores`: Logical cores assigned to container or JVM (`-XX:ActiveProcessorCount`)
- `CPU Usage per Thread`: ~1.0 for CPU-bound, ~0.1–0.3 for I/O-bound threads

> This adjusted formula gives a more **realistic thread pool size** that considers resource contention and JVM internals.

---



Even in isolated containers:

### 1. JVM memory ≠ only heap
- `-Xmx` defines the heap, but JVM also uses:
  - Metaspace (class metadata)
  - Thread stacks (default ~1MB/thread)
  - DirectByteBuffers (NIO, Netty, Kafka)
  - JIT compiler memory, GC internal allocations

### 2. GC and JIT need free CPU
- GC (like G1 or ZGC) runs concurrently and consumes CPU.
- JIT compilation (C1/C2) compiles methods at runtime.
- Using 100% CPU = GC starved ➝ long GC pause or `Full GC`.

### 3. Headroom is necessary
- OS signal handling, logging, class loading, GC threads
- Dynamic thread pools (Spring `@Async`, HTTP thread pools)
- Direct memory leaks (Netty, Kafka) can kill container via OOM

---

## 🧱 Recommended JVM Resource Allocation in Containers

| Resource  | Recommended % of Container Limit        | Why?                                                         |
|-----------|-----------------------------------------|--------------------------------------------------------------|
| CPU       | 50%–70%                                 | Allow room for GC, JIT, async threads                        |
| RAM       | 60%–75%                                 | Leave space for native memory, direct buffers, thread stacks |
| Threads   | Use formula above, avoid over-threading | Prevent excessive memory/thread usage                        |

---

## ⚙️ General JVM Flags for Containers

```bash
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=65
-XX:ActiveProcessorCount=2
-XX:+UseG1GC
-XX:+UseStringDeduplication
-XX:+AlwaysPreTouch
-XX:+HeapDumpOnOutOfMemoryError
```

---

## 🧪 Recommended Setup per Use Case

### ✅ 1. Web/API Services (Spring Boot, REST, gRPC)

| Resource         | Recommendation               |
|------------------|------------------------------|
| CPU              | 50–70%                       |
| Heap             | 60–70% of RAM                |
| GC               | G1GC or ZGC                  |
| Thread Pool      | `Cores × (1 + Wait/Compute)` |
| GC Threads       | `Cores / 2`                  |

**Why**: Mixed I/O + CPU tasks, requires balance between responsiveness and throughput.

**Flags**:

```bash
-Xmx8G -Xms8G
-XX:+UseG1GC
-XX:MaxRAMPercentage=65
-XX:ActiveProcessorCount=4
```

---

### ✅ 2. Batch/ETL/Job Processing (Kafka consumer, Airflow, scheduler)

| Resource         | Recommendation                  |
|------------------|---------------------------------|
| CPU              | 70–90%                          |
| Heap             | 70–80% of RAM                   |
| GC               | ParallelGC or G1GC              |
| Thread Model     | ForkJoinPool or thread-per-task |
| GC Threads       | Equals number of cores          |

**Why**: High throughput with less concern for latency; jobs are predictable and often CPU-heavy.

**Flags**:

```bash
-Xmx16G -Xms16G
-XX:+UseParallelGC
-XX:ActiveProcessorCount=6
```

---

### ✅ 3. Low-Latency / Real-Time Systems (Trading, Scoring, Realtime APIs)

| Resource         | Recommendation       |
|------------------|----------------------|
| CPU              | 40–60%               |
| Heap             | Small (1–4GB)        |
| GC               | ZGC or Shenandoah    |
| Thread Pool      | Fixed, minimal       |
| GC Threads       | As small as possible |

**Why**: Must minimize GC pause and jitter, no tolerance for Full GC.

**Flags**:

```bash
-Xmx2G -Xms2G
-XX:+UseZGC
-XX:MaxRAMPercentage=50
```

---

### ✅ 4. Kafka Consumers / Worker Services

| Resource         | Recommendation                            |
|------------------|-------------------------------------------|
| CPU              | 60–80%                                    |
| Heap             | 4–8GB                                     |
| GC               | G1GC                                      |
| Thread Model     | Consumer-per-partition or per-topic-group |
| GC Threads       | Core / 2                                  |

**Why**: Balanced throughput and responsiveness, async I/O workloads.

**Flags**:

```bash
-Xmx4G -Xms4G
-XX:+UseG1GC
-XX:MaxRAMPercentage=70
```

---

## 📊 GC Tuning Best Practices (for G1GC)

```bash
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
-XX:+ParallelRefProcEnabled
-XX:InitiatingHeapOccupancyPercent=30
-XX:+HeapDumpOnOutOfMemoryError
-Xlog:gc*:file=/app/logs/gc.log:time,uptime,level,tags
```

---

## 📈 Monitoring and Validation Tools

| Tool                   | Purpose                           |
|------------------------|-----------------------------------|
| `jstat`, `jcmd`        | GC and heap stats                 |
| `jvisualvm`, JFR       | Thread and memory analysis        |
| `top`, `htop`          | System CPU/RAM monitoring         |
| `prometheus + grafana` | Time series JVM metrics           |
| `wrk`, `jmeter`        | Load and latency testing          |

---

## 🐳 Example Dockerfile for Spring Boot

```dockerfile
FROM eclipse-temurin:21-jdk as runtime

WORKDIR /app
COPY target/your-app.jar app.jar

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=65 -XX:ActiveProcessorCount=2 -XX:+UseContainerSupport"

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
```

---

## 🎯 Conclusion

Even in isolated environments like containers:

- **Never allocate 100% CPU/RAM to the JVM**.
- JVM uses native memory and extra threads you don’t see with `-Xmx`.
- Leave 30–40% headroom to ensure:
  - Stable GC
  - No out-of-memory kills
  - Responsive thread scheduling

Always benchmark your service under realistic load before scaling out.