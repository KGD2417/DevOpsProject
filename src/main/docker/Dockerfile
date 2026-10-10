FROM eclipse-temurin:21-jdk-focal as build
WORKDIR /source
COPY . .
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:21-jre-focal
WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser -m -d /home/appuser appuser

# Copy JAR from build stage
COPY --from=build /source/target/streetlight-fault-reporting-1.0.0-SNAPSHOT.jar app.jar

# Set ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Entry point
ENTRYPOINT ["java", "-jar", "app.jar"]