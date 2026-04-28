# Stage 1: Build the application using Gradle
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

# Copy the Gradle files first to cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Copy the source code
COPY src ./src

# Build the shadow JAR (fat jar)
RUN gradle shadowJar --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Expose dynamic port for Ktor (Railway will map this)
EXPOSE $PORT

# Copy the built shadow JAR from the builder stage
COPY --from=builder /app/build/libs/*-all.jar ./app.jar

# Run the jar file, reading Railway's PORT environment variable
CMD ["sh", "-c", "java -jar app.jar -port=${PORT:-8080}"]
