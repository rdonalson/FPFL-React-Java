#FROM ubuntu:latest
#LABEL authors="rickd"
#
#ENTRYPOINT ["top", "-b"]

# ============================
# 1. Build Stage
# ============================
FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /app

# Copy root pom and module poms first for caching
COPY pom.xml .
COPY module-api/pom.xml module-api/
COPY module-common-bc/pom.xml module-common-bc/
COPY module-display-bc/pom.xml module-display-bc/
COPY module-items-bc/pom.xml module-items-bc/

# Pre-download dependencies (massive speed boost)
RUN mvn -q -e -B dependency:go-offline

# Copy full source
COPY . .

# Build the Boot JAR
RUN mvn -q -e -B clean package -DskipTests


# ============================
# 2. Runtime Stage
# ============================
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/module-api/target/module-api-1.0.0.jar app.jar

# Expose your WebFlux port
EXPOSE 8010

# Run the service
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
