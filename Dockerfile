# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn clean package -DskipTests -B -q

# Rename to a fixed name so stage 2 doesn't need a wildcard
RUN mv target/*.jar target/app.jar

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/app.jar app.jar

EXPOSE 8080

# Shell form so $PORT is expanded at container startup
CMD java \
  -Xms64m \
  -Xmx256m \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=60.0 \
  -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  -Dserver.port=${PORT:-8080} \
  -jar app.jar