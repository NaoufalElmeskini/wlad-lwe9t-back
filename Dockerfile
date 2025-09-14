FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="WladLwe9t Team"
LABEL description="WladLwe9t REST API application with PostgreSQL"

# Create application user
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -s /bin/sh -D appuser

# Set working directory
WORKDIR /app

# Copy built JAR file
COPY target/wladLwe9t-*.jar app.jar

# Change ownership to application user
RUN chown -R appuser:appgroup /app

# Switch to application user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/status || exit 1

# Set active profile for Docker
ENV SPRING_PROFILES_ACTIVE=docker

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]