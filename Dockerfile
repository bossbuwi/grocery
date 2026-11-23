# --- Stage 1: Build the Application ---
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy dependency descriptors first to leverage Docker layer caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

# Copy the source code and package the application
COPY src src
RUN ./mvnw package -DskipTests

# --- Stage 2: Minimal Runtime Environment ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the compiled jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port and run the application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]