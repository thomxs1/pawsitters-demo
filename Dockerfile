# =============================================================
# Build-Stage: Maven kompiliert das Spring-Boot-JAR
# =============================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Dependencies zuerst kopieren, fuer Layer-Caching
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

# Quellcode kopieren und bauen (ohne Tests, die laufen in der CI)
COPY src ./src
RUN mvn -B -ntp package -DskipTests

# =============================================================
# Runtime-Stage: schlankes JRE-Image, nur das fertige JAR
# =============================================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# JAR aus Build-Stage uebernehmen
COPY --from=build /app/target/*.jar app.jar

# Render & Co. setzen die PORT-Env, Spring Boot soll darauf hoeren
EXPOSE 8080
ENV PORT=8080

# Health-Hint fuer Container-Plattformen: JVM mit sinnvollen Defaults
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
