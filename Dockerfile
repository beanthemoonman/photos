# Multi-stage build for Photos application

# Build stage
FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY .git .git
COPY pom.xml .
COPY src src

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# Create volume for photos directory
VOLUME /app/photos

# Copy the built artifact from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Set the command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]