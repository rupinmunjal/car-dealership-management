# Build Angular using a supported LTS Node release.
FROM node:22-alpine AS frontend-build

WORKDIR /workspace/src/main/webapp

COPY src/main/webapp/package.json src/main/webapp/package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY src/main/webapp/ ./
RUN npm run build

# Package Spring Boot with the Angular output generated above.
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
COPY --from=frontend-build /workspace/src/main/resources/static ./src/main/resources/static
RUN mvn package -DskipTests -B

# Run the combined application as an unprivileged user.
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S -g 1001 appgroup \
    && adduser -S -D -H -u 1001 -G appgroup appuser

WORKDIR /app

COPY --from=backend-build /workspace/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
