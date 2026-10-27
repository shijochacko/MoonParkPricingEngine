# Build from source; no host Java or Gradle installation is required.
FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/
COPY src/ src/

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew \
    && ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app app

COPY --from=build --chown=app:app /workspace/build/libs/app.jar app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 \
    CMD curl --fail --silent --show-error "http://localhost:${PORT:-8080}/actuator/health/readiness" > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
