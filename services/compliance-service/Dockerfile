# ── Build stage ──────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /workspace

# Layer 1: POMs only (cached unless dependencies change)
COPY pom.xml .
COPY shared/asd-events/pom.xml  shared/asd-events/
COPY shared/asd-common/pom.xml  shared/asd-common/
COPY services/compliance-service/pom.xml services/compliance-service/
RUN mvn dependency:go-offline     -pl shared/asd-events,shared/asd-common,services/compliance-service -am -q

# Layer 2: sources + build
COPY shared/ shared/
COPY services/compliance-service/ services/compliance-service/
RUN mvn clean package -pl services/compliance-service -am -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S asd && adduser -S asd -G asd
USER asd
COPY --from=build /workspace/services/compliance-service/target/*.jar app.jar

# Virtual threads: enabled by default in Java 25 with Spring Boot 3.5
ENTRYPOINT ["java",   "-XX:+UseContainerSupport",   "-XX:MaxRAMPercentage=75.0",   "-Dspring.threads.virtual.enabled=true",   "-jar", "app.jar"]
