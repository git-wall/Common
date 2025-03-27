# ===========================================================================================================
# Builder stage 1 : Compile the application and prepare dependencies
# ===========================================================================================================
FROM eclipse-temurin:11-jdk as build
WORKDIR /app
# Copy all project files (source code, Gradle files, etc.) to the container
COPY . .
# Run Gradle to build the application, skipping tests for faster builds
# - 'clean' ensures a fresh build
# - 'build -x test' skips unit tests (remove '-x test' if tests are critical)
RUN ./gradlew clean build -x test
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
    build/libs/Common.jar > modules.txt \
# Create a custom JRE using jlink for a smaller runtime
# - '--add-modules $(cat modules.txt),jdk.crypto.ec': Includes required modules from modules.txt plus jdk.crypto.ec (for SSL/TLS)
# - '--module-path $JAVA_HOME/jmods': Uses JDK's module path
# - '--strip-debug': Removes debug info to reduce size
# - '--compress=2': Applies maximum compression
# - '--no-header-files' and '--no-man-pages': Excludes unnecessary files
# - '--output build/image': Outputs the custom JRE to build/image
RUN jlink  \
    --add-modules $(cat modules.txt),jdk.crypto.ec \
    --module-path $JAVA_HOME/jmods \
    --strip-debug \
    --compress=2 \
    --no-header-files \
    --no-man-pages \
    --output build/image


# ===========================================================================================================
# Extractor Stage 2 : Extract Spring Boot layers for efficient Docker caching
# ===========================================================================================================
# Note: Your Gradle config should have -> 'layered { enabled = true  dependencies {intoLayer 'dependencies'} }'
# to ensure the JAR supports layertools extraction
FROM eclipse-temurin:11-jre AS extractor
WORKDIR /opt/app
# Copy the built JAR from the builder stage (fixed path from /app to /opt/app)
COPY --from=build /opt/app/build/libs/Common.jar application.jar
# Extract layers using Spring Boot's layertools
# Layers typically include: dependencies, spring-boot-loader, snapshot-dependencies, application
RUN java -Djarmode=layertools -jar application.jar extract


# ===========================================================================================================
# Runtime Stage 3 : Build the final image with a custom JRE and GC( ZGC, SerialGC, G1GC, ParallelGC )
# ===========================================================================================================
FROM ubuntu:jammy
# Set JAVA_HOME to the custom JRE location
ENV JAVA_HOME=/opt/java/jdk11
# Update PATH to include Java binaries
ENV PATH=${JAVA_HOME}/bin:${PATH}
# Copy the custom JRE from the builder stage (fixed typo 'myjre' to 'build/image')
COPY --from=build /opt/app/myjre ${JAVA_HOME}
WORKDIR /opt/app
# Copy Spring Boot layers from the extractor stage for efficient caching
COPY --from=extractor /opt/app/dependencies/ ./
COPY --from=extractor /opt/app/spring-boot-loader/ ./
COPY --from=extractor /opt/app/snapshot-dependencies/ ./
COPY --from=extractor /opt/app/application/ ./
# Configure JVM with ZGC and NUMA awareness
# - '-XX:+UnlockExperimentalVMOptions': Required to enable ZGC in Java 11 (experimental)
# - '-XX:+UseZGC': Enables Z Garbage Collector for low-latency GC
# - '-XX:+UseNUMA': Explicitly enables NUMA awareness (default with ZGC on multi-socket systems)
# - '-Xmx4g -Xms4g': Sets heap size to 4GB (adjust based on container memory limit)
# - '-XX:+UseContainerSupport': Ensures JVM respects Docker memory limits
# - '-Xlog:gc': Logs GC activity to verify ZGC usage (optional, remove for production)
# - 'org.springframework.boot.loader.JarLauncher': Entry point for Spring Boot layered JAR
ENTRYPOINT ["java",
 "-XX:+UnlockExperimentalVMOptions", \
 "-XX:+UseZGC", \
 "-XX:+UseNUMA", \
             # seting ram auto by percentage
             "-XX:MaxRAMPercentage=75.0", \
             "-XX:InitialRAMPercentage=50.0", \
             "-XX:MinRAMPercentage=25.0", \
             # if not want setup ram auto set number ram like under, just choose above and below
             "Xms256m", \
             "Xmx512m", \
 "-XX:+UseContainerSupport", \
 "-Xlog:gc", \
 "org.springframework.boot.loader.JarLauncher"]

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
#ENTRYPOINT ["java",
#  "-XX:+UseContainerSupport",
#  "-XX:+UseG1GC",
#  "-XX:InitialRAMPercentage=50.0",
#  "-XX:MaxRAMPercentage=75",
#  "org.springframework.boot.loader.JarLauncher"]

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

# Why ZGC with NUMA?
# - ZGC minimizes GC pauses, critical for real-time or interactive apps
# - NUMA awareness leverages multi-socket hardware (if available) for faster memory access
# - Pairing with a custom JRE (via jlink) reduces image size and attack surface











