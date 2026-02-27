# Runtime only — no Maven, no internet needed during docker-compose build.
# Pre-build the JAR locally first: mvn clean package -DskipTests
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S asd && adduser -S asd -G asd
USER asd
COPY services/bff-member/target/bff-member-*.jar app.jar
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Dspring.threads.virtual.enabled=true", \
  "-jar", "app.jar"]
