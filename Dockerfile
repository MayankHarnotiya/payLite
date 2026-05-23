# Stage 1: Build the JAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY wallet-service ./wallet-service
RUN mvn -pl wallet-service package -DskipTests -q

# Stage 2: Run the JAR
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/wallet-service/target/*.jar app.jar

# Railway sets PORT env var; Spring reads it from application.yml
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]