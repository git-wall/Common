# Tên file JAR thường nằm trong build/libs/
JAR_PATTERN=build/libs/*.jar

# Build file JAR (chạy local hoặc trong Docker)
build-jar:
	@echo "--- Building JAR with Gradle ---"
	./gradlew clean bootJar -x test

# Build Docker Image
docker-build:
	@echo "--- Building Docker Image ---"
	docker build -t my-app-gradle .

# Chạy thử dưới máy local
run-local:
	./gradlew bootRun

.PHONY: build-jar docker-build run-local
