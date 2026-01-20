# Multi-stage build

FROM maven:3.9-eclipse-temurin-11 AS builder
WORKDIR /project
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /project/target/task-tracker-1.0-SNAPSHOT.jar app.jar
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
