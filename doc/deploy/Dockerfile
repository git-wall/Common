FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

COPY app.jar app.jar

RUN useradd -u 1000 -m appuser
USER appuser

ENTRYPOINT ["java",
  "-XX:+UseG1GC",
  "-XX:+UseContainerSupport",
  "-XX:MaxRAMPercentage=50.0",
  "-XX:InitialRAMPercentage=50.0",
  "-XX:+ExitOnOutOfMemoryError",
  "-jar",
  "app.jar"
]
