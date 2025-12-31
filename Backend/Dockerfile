# --- Build Stage ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy Maven wrapper and build files first (to leverage Docker cache)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Give permissions to mvnw
RUN chmod +x mvnw

# Download dependencies (faster build next time)
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the project
COPY src ./src

# Build the project
RUN ./mvnw clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
