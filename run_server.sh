#!/bin/bash
./apache-maven-3.9.12/bin/mvn clean package -DskipTests -q
echo "Starting Weather Server on http://localhost:8080..."
./apache-maven-3.9.12/bin/mvn exec:java -Dexec.mainClass="com.example.weather.WeatherServer" -q
