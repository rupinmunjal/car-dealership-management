# ============================================
# Dockerfile Template - Java/Spring Application
# (Adapted for Java 21)
# ============================================

# Stage 1: Build
# CHANGED: Using Java 21 to match your project version
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
# CHANGED: Using Java 21 Runtime
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001

WORKDIR /app

# Copy the built jar from build stage
# This finds any .jar file in target/ and renames it to app.jar
COPY --from=build /app/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

# Exposed port (Internal Container Port)
EXPOSE 8080

# Health check
# Uses the Actuator endpoint we enabled in SecurityConfig
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Start command
CMD ["java", "-jar", "app.jar"]