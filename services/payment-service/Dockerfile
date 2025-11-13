# Multi-stage build for optimized production image
FROM maven:3.9.4-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first (for dependency caching)
COPY pom.xml .

# Download dependencies with cache mount (this layer will be cached if pom.xml doesn't change)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with cache mount
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# Production stage - using Eclipse Temurin for reliable Java runtime
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Create non-root user
RUN groupadd -g 1001 appuser && \
    useradd -r -u 1001 -g appuser -d /app -s /bin/bash appuser

# Copy the jar file from builder stage
COPY --from=builder /app/target/payment-service-*.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8082

# JVM options for containerized environment
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseContainerSupport"

# Run the application
CMD ["java", "-jar", "/app/app.jar"]
