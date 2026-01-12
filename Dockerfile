# Stage 1: Build the Java application
# We use a Docker image that has Maven pre-installed
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the jar file (skip tests to speed it up)
# The output jar will be in /app/target/
RUN mvn clean package -DskipTests

# Stage 2: Run the application
# We use a lighter image (JRE execution only) for the final container
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR file from the 'build' stage
# Make sure the name matches what's in pom.xml (global-weather-bridge-1.0-SNAPSHOT.jar)
COPY --from=build /app/target/global-weather-bridge-1.0-SNAPSHOT.jar /app/server.jar

# Expose port 8080 (where our server listens)
EXPOSE 8080

# Command to run the application when the container starts
CMD ["java", "-jar", "/app/server.jar"]
