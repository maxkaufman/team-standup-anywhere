# Multi-stage build for Spring Boot
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B 2>/dev/null || true

# Build
COPY src src
RUN mvn clean package -DskipTests -q

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
