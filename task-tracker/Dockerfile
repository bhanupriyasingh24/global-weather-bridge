FROM openjdk:11-jdk-slim

WORKDIR /app

# Copy the jar file (assumes mvn package has run, or use multistage build)
# Using a multistage build is better practice for standalone Dockerfile
COPY pom.xml .
COPY src ./src
COPY run_server.sh .

# Install maven (or use wrapper if I had one, but I'll assume system maven for local dev or use a builder image)
# Let's verify what the user has. The user has maven locally. 
# For a robust Dockerfile, I'll use a maven image to build.

FROM maven:3.9-eclipse-temurin-11 AS builder
WORKDIR /project
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /project/target/task-tracker-1.0-SNAPSHOT.jar app.jar
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
