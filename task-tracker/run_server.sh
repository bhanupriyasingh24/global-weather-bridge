#!/bin/bash
cd "$(dirname "$0")"

# Ensure Maven wrapper or local maven is available
echo "Building Task Tracker..."
/Users/bhanupriyasingh/Documents/Myprojects/apache-maven-3.9.12/bin/mvn clean package

if [ $? -eq 0 ]; then
    echo "Build successful. Starting server..."
    java -jar target/task-tracker-1.0-SNAPSHOT.jar
else
    echo "Build failed."
fi
