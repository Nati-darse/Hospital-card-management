
# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the Spring Boot jar
RUN mvn clean package -DskipTests

# ---------- Run stage ----------
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
