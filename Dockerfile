# ================================
# Stage 0: Build with Gradle
# ================================
FROM gradle:8.5-jdk11 AS builder

WORKDIR /build

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies

COPY src src

RUN ./gradlew clean bootJar --no-daemon

#==============================================================================
#     Builder stage 1 : Compile the application and prepare dependencies
#==============================================================================
# Use version ap or
FROM eclipse-temurin:11-jdk as deps

# set work app in docker
WORKDIR /deps

# Copy all project files (source code, Gradle files, etc.) to the container
COPY --from=builder /build/build/libs/*.jar app.jar

# Analyze dependencies with jdeps to minimize runtime image size
# - '--ignore-missing-deps': Ignores unresolved dependencies (useful for optional libs)
# - '-q': Quiet mode, reduces output noise
# - '--recursive': Scans all dependencies recursively
# - '--multi-release 11': Ensures compatibility with Java 11 multi-release JARs
# - '--class-path "build/libs/*"': Includes all JARs in build/libs for analysis
# - '--print-module-deps': Outputs a comma-separated list of required modules
# - Result is written to modules.txt
RUN jdeps  \
    --ignore-missing-deps -q \
    --recursive \
    --multi-release 11 \
    --class-path "build/libs/*" \
    --print-module-deps  \
    app.jar > modules.txt

# Create a custom JRE using jlink for a smaller runtime
# - '--add-modules $(cat modules.txt),jdk.crypto.ec': Includes required modules from modules.txt plus jdk.crypto.ec (for SSL/TLS)
# - '--module-path $JAVA_HOME/jmods': Uses JDK's module path
# - '--strip-debug': Removes debug info to reduce size
# - '--compress=2': Applies maximum compression
# - '--no-header-files' and '--no-man-pages': Excludes unnecessary files
# - '--output build/image': Outputs the custom JRE to build/image
RUN jlink  \
    --add-modules $(cat modules.txt),jdk.crypto.ec \
    --module-path /opt/java/jmods \
    --strip-debug \
    --compress=2 \
    --no-header-files \
    --no-man-pages \
    --output /appjre

# ===========================================================================================================
# Run stage 2 : Compile the application and run
# ===========================================================================================================
FROM debian:bullseye-slim
# or bookworm-slim

ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

# Linux Alpine
# RUN apk add --no-cache make
# CentOS/RHEL/Oracle Linux
# RUN microdnf install make && microdnf clean all
# Ubuntu / Debian
RUN apt-get update  \
    && apt-get install -y make  \
    && rm -rf /var/lib/apt/lists/* # Clean up apt cache to reduce image size

# -u	User ID	Giúp quản lý quyền ghi file chính xác.
# -r	System account	Bảo mật hơn, gọn nhẹ vì không tạo home folder.
# -m	Tự động tạo thư mục cá nhân tại /home/thanh ,	Cần thiết nếu bạn muốn lưu cài đặt cá nhân cho user.
RUN useradd -r -u 1001 thanh
# /appjre output from jlink
COPY --from=deps /appjre $JAVA_HOME

WORKDIR /app

COPY --from=builder  /build/build/libs/*.jar app.jar
# run app with user was create
RUN chown -R thanh:thanh /app
# active user
USER thanh

# Configure JVM with ZGC and NUMA awareness
# - '-XX:+UnlockExperimentalVMOptions': Required to enable ZGC in Java 11 (experimental)
# - '-XX:+UseZGC': Enables Z Garbage Collector for low-latency GC
# - '-XX:+UseNUMA': Explicitly enables NUMA awareness (default with ZGC on multi-socket systems)
# - '-Xmx4g -Xms4g': Sets heap size to 4GB (adjust based on container memory limit)
# - '-XX:+UseContainerSupport': Ensures JVM respects Docker memory limits
# - '-Xlog:gc': Logs GC activity to verify ZGC usage (optional, remove for production)
# - 'org.springframework.boot.loader.JarLauncher': Entry point for Spring Boot layered JAR
ENTRYPOINT ["java",
# "-XX:+UnlockExperimentalVMOptions", \
 "-XX:+UseG1GC", \
# "-XX:+UseNUMA", \
             # seting ram auto by percentage
#             "-XX:MaxRAMPercentage=75.0", \
#             "-XX:InitialRAMPercentage=50.0", \
#             "-XX:MinRAMPercentage=25.0", \
             # but with setting dynamic make jvm will stop sometime for change setting for get more ram
             # so you if you are good and controll ok setting all is same value
             # if not you may be meet problem about OOM
             # if me use one "-Xmx512m" is ok this like hard code but make everything fast and ok
             # but what number you need to use => 50% of RAM in container
             "-Xms256m", \
             "-Xmx512m", \
  "-XX:+UseContainerSupport", \
 "-XX:+ExitOnOutOfMemoryError", \
 "-jar", "app.jar"
# "-Xlog:gc", \
# "org.springframework.boot.loader.JarLauncher"
 ]

# For java 17+
#ENTRYPOINT ["java", \
#     "-XX:+UseZGC", \
#     "-XX:+UseNUMA", \
#     "-XX:MaxRAMPercentage=75.0", \
#     "-XX:InitialRAMPercentage=50.0", \
#     "-XX:MinRAMPercentage=25.0", \
#     "-XX:+UseContainerSupport", \
#     "-XX:ZUncommitDelay=300", \
#     "-XX:+UseLargePages", \
#     "-XX:+UseStringDeduplication", \
#     "-Xlog:gc", \
#     "org.springframework.boot.loader.JarLauncher"]

# Config JVM with G1GC

#  "-XX:+UseContainerSupport"
#  "-XX:+UseG1GC"
#  "-Xms256m"
#  "-Xmx512m"
#  "-XX:+ExitOnOutOfMemoryError"


# Optimization Notes:
# - Memory Tuning:
#   - '-Xms' (initial heap) and '-Xmx' (max heap) are set explicitly to avoid resizing overhead
#   - Example: For 16GB container memory, '-Xms8g -Xmx12g' would align with 50%-75% usage
#   - Current setting: 4GB heap, suitable for containers with ~6GB memory limit
# - ZGC Benefits:
#   - Ultra-low pause times (<10ms), ideal for latency-sensitive apps
#   - Scales well with large heaps (tested up to terabytes)
#   - NUMA awareness improves performance on multi-socket systems by localizing memory access
# - Container Considerations:
#   - Use 'docker run -m 6g --cpus="2"' to ensure ZGC and NUMA have sufficient resources
#   - Avoid CPU pinning (e.g., '--cpuset-cpus="0"') to allow NUMA across nodes

# Alternative GC Options (uncomment to replace ZGC if needed):
# 1. SerialGC (-XX:+UseSerialGC):
#    - Low overhead, best for small heaps (<4GB), single-threaded GC
#    - Use: Replace '-XX:+UseZGC' with '-XX:+UseSerialGC' (remove UnlockExperimentalVMOptions)
# 2. G1GC (-XX:+UseG1GC):
#    - Balanced throughput and latency, good for heaps <16GB, long-lived services
#    - Use: Replace '-XX:+UseZGC' with '-XX:+UseG1GC' (remove UnlockExperimentalVMOptions)
# 3. ParallelGC (-XX:+UseParallelGC):
#    - High throughput, suitable for batch jobs, tolerates longer pauses
#    - Use: Replace '-XX:+UseZGC' with '-XX:+UseParallelGC' (remove UnlockExperimentalVMOptions)
# 4. CMS (-XX:+UseConcMarkSweepGC):
#    - Legacy concurrent GC, lower pauses than ParallelGC, deprecated in Java 9+
#    - Use: Replace '-XX:+UseZGC' with '-XX:+UseConcMarkSweepGC' (remove UnlockExperimentalVMOptions)

# Why ZGC with NUMA
# - ZGC minimizes GC pauses, critical for real-time or interactive apps
# - NUMA awareness leverages multi-socket hardware (if available) for faster memory access
# - Pairing with a custom JRE (via jlink) reduces image size and attack surface











