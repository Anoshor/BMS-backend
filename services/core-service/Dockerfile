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

# Production stage - using distroless for smaller, more secure image
FROM gcr.io/distroless/java17-debian12

# Set working directory
WORKDIR /app

# Copy the jar file from builder stage
COPY --from=builder /app/target/bms-backend-*.jar app.jar

# Create data directory for H2 database
# Note: distroless doesn't have mkdir, so we use COPY to create structure
COPY --from=builder --chown=nonroot:nonroot /tmp /app/data

# Switch to non-root user (distroless has nonroot user by default)
USER nonroot

# Expose port
EXPOSE 8080

# JVM options for containerized environment
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseContainerSupport"

# Run the application
CMD ["java", "-jar", "/app/app.jar"]