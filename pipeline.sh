#!/bin/bash

# Fail on any error, undefined variable, or pipeline failures
set -euo pipefail

# Enable Docker BuildKit for faster parallel builds
export DOCKER_BUILDKIT=1

# Set Docker build context to project root
BUILD_CONTEXT=".."

# Define image name and tag
IMAGE_NAME="common-app"
TAG="latest"
if [ -n "${1:-}" ]; then
    TAG="$1"
fi

HOSTNAME="localhost"
if [ -n "${2:-}" ]; then
    HOSTNAME="$2"
fi

# Container name for running instance
CONTAINER_NAME="${IMAGE_NAME}-instance"

# Application port - change as needed
APP_PORT=8080

# Prepare build-time variables
GRADLE_ARGS="--no-daemon --parallel --build-cache"
BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

echo "[INFO] Building ${IMAGE_NAME}:${TAG} with optimized settings..."

# Run Docker build process
function run_build() {
    # Run Gradle build to prepare application
    echo "[INFO] Running Gradle build..."
    (cd "${BUILD_CONTEXT}" && ./gradlew build ${GRADLE_ARGS})

    # Docker build with optimizations
    echo "[INFO] Building Docker image with optimizations..."
    docker build "${BUILD_CONTEXT}" \
      --file "${BUILD_CONTEXT}/Dockerfile" \
      --tag "${IMAGE_NAME}:${TAG}" \
      --build-arg BUILDKIT_INLINE_CACHE=1 \
      --build-arg "GRADLE_ARGS=${GRADLE_ARGS}" \
      --build-arg "BUILD_DATE=${BUILD_DATE}" \
      --progress=plain \
      --cache-from "${IMAGE_NAME}:cache" \
      --cache-from "${IMAGE_NAME}:latest" \
      --cache-from "${IMAGE_NAME}:${TAG}" \
      --network=host \
      --output type=docker

    # Save the image for export (if needed)
    echo "[INFO] Saving build cache..."
    docker tag "${IMAGE_NAME}:${TAG}" "${IMAGE_NAME}:cache"

    echo "[INFO] Build complete! Image: ${IMAGE_NAME}:${TAG}"

    # Display image details
    echo "[INFO] Image details:"
    docker image inspect "${IMAGE_NAME}:${TAG}" --format "{{.Size}}"
}

function clean_everything() {
    echo "[INFO] Performing thorough cleanup of build environment..."

    # Clean Gradle artifacts
    echo "[INFO] Cleaning Gradle artifacts..."
    (cd "${BUILD_CONTEXT}" && ./gradlew clean --no-daemon)

    # Remove Gradle cache and temp files
    echo "[INFO] Removing Gradle cache..."
    rm -rf "${BUILD_CONTEXT}/.gradle" 2>/dev/null || true
    rm -rf "${BUILD_CONTEXT}/build" 2>/dev/null || true

    # Clean Docker build cache
    echo "[INFO] Cleaning Docker build cache..."
    docker builder prune -f

    # Remove dangling images
    echo "[INFO] Removing dangling images..."
    docker image prune -f

    echo "[INFO] Environment is now clean!"
}

function run_container_securely() {
    echo "[INFO] Preparing to run container securely..."

    # Stop and remove any existing container with the same name
    docker stop "${CONTAINER_NAME}" 2>/dev/null || true
    docker rm "${CONTAINER_NAME}" 2>/dev/null || true

    echo "[INFO] Starting container with security best practices..."

    # Run container with security best practices
    docker run -d \
      --name "${CONTAINER_NAME}" \
      --restart unless-stopped \
      --publish ${APP_PORT}:${APP_PORT} \
      --cap-drop ALL \
      --cap-add NET_BIND_SERVICE \
      --security-opt no-new-privileges \
      --read-only \
      --tmpfs /tmp:rw,noexec,nosuid \
      --memory=1g \
      --cpu-shares=1024 \
      --health-cmd "curl -f http://${HOSTNAME}:${APP_PORT}/health || exit 1" \
      --health-interval=30s \
      --health-retries=3 \
      --health-timeout=5s \
      "${IMAGE_NAME}:${TAG}"

    echo "[INFO] Container started with enhanced security!"
    echo "[INFO] Container details:"
    docker ps --filter "name=${CONTAINER_NAME}" --format "ID: {{.ID}}\nName: {{.Names}}\nStatus: {{.Status}}\nPorts: {{.Ports}}"

    # Wait for container to be healthy
    echo "[INFO] Waiting for container to be healthy..."
    for i in {1..3}; do
        if [ "$(docker inspect --format='{{.State.Health.Status}}' ${CONTAINER_NAME} 2>/dev/null)" == "healthy" ]; then
            echo "[INFO] Container is healthy and running!"
            break
        fi
        echo "[INFO] Waiting for container to become healthy... (${i}/3)"
        sleep 3
    done
}

# Run the build process
run_build

# Run the cleanup process
clean_everything

# Run the container securely
run_container_securely

echo "[INFO] Pipeline completed successfully! Your application is now running securely."