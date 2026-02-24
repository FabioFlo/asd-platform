# ── Build stage ──────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /workspace

# Layer 1: POMs only (cached unless dependencies change)
COPY pom.xml .
COPY shared/asd-events/pom.xml  shared/asd-events/
COPY shared/asd-common/pom.xml  shared/asd-common/
COPY services/competition-service/pom.xml services/competition-service/
RUN mvn dependency:go-offline     -pl shared/asd-events,shared/asd-common,services/competition-service -am -q

# Layer 2: sources + build
COPY shared/ shared/
COPY services/competition-service/ services/competition-service/
RUN mvn clean package -pl services/competition-service -am -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S asd && adduser -S asd -G asd
USER asd
COPY --from=build /workspace/services/competition-service/target/*.jar app.jar

# Virtual threads: enabled by default in Java 25 with Spring Boot 3.5
ENTRYPOINT ["java",   "-XX:+UseContainerSupport",   "-XX:MaxRAMPercentage=75.0",   "-Dspring.threads.virtual.enabled=true",   "-jar", "app.jar"]
