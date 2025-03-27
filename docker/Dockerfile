FROM eclipse-temurin:21-jdk AS build
WORKDIR /opt/app
COPY . .
RUN ./gradlew clean build -x test

RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 21 \
    --print-module-deps \
    --class-path 'build/libs/dependencies/*' \
    build/libs/Common.jar > deps.info

RUN jlink \
    --add-modules $(cat deps.info) \
    --strip-debug \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output myjre

FROM eclipse-temurin:21-jre AS extractor
WORKDIR /opt/app
COPY --from=build /opt/app/build/libs/Common.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM ubuntu:jammy
ENV JAVA_HOME=/opt/java/jdk21
ENV PATH=${JAVA_HOME}/bin:${PATH}
COPY --from=build /opt/app/myjre ${JAVA_HOME}
WORKDIR /opt/app
COPY --from=extractor /opt/app/dependencies/ ./
COPY --from=extractor /opt/app/spring-boot-loader/ ./
COPY --from=extractor /opt/app/snapshot-dependencies/ ./
COPY --from=extractor /opt/app/application/ ./
ENTRYPOINT ["java", "-XX:+UseParallelGC", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.JarLauncher"]