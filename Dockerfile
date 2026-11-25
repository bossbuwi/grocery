# --- Stage 1: Build the Application ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy dependency descriptors first to leverage Docker layer caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY frontend frontend

# Make the Maven wrapper executable
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source code and package application
COPY src src
RUN ./mvnw package -DskipTests

# --- Stage 2: Minimal Runtime Environment ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy only the compiled jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]