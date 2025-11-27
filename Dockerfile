FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven

# First build banking-core
COPY banking-core ./banking-core
WORKDIR /app/banking-core
RUN mvn clean package -DskipTests

# Copy banking-core JAR to netbanking-app lib directory
WORKDIR /app
COPY netbanking-app ./netbanking-app
RUN cp banking-core/target/banking-core-*.jar netbanking-app/lib/

# Build netbanking-app
WORKDIR /app/netbanking-app
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /app/netbanking-app/target/netbanking-app-*.jar netbanking-app.jar

# Create non-root user
RUN groupadd -r banking && useradd -r -g banking banking

# Create logs directory
RUN mkdir -p logs && chown -R banking:banking logs

USER banking

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "netbanking-app.jar"]
