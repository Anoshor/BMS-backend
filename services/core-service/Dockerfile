# Multi-stage build for optimized production image
FROM openjdk:17-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy pom.xml first (for dependency caching)
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN groupadd -r bmsapp && useradd -r -g bmsapp bmsapp

# Copy the jar file from builder stage
COPY --from=builder /app/target/bms-backend-*.jar app.jar

# Create data directory for H2 database
RUN mkdir -p /app/data && chown -R bmsapp:bmsapp /app

# Switch to non-root user
USER bmsapp

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]