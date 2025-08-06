#!/bin/bash

# Qwen3 AI Integration Startup Script
# This script sets up and runs the Qwen3 AI integration application

echo "Starting Qwen3 AI Integration..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 11 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
if [[ $(echo "$JAVA_VERSION >= 11" | bc -l) -eq 0 ]]; then
    echo "Error: Java 11 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "Java version: $JAVA_VERSION"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven."
    exit 1
fi

echo "Maven found: $(mvn --version | head -n 1)"

# Set environment variables
export QWEN3_API_KEY=${QWEN3_API_KEY:-"your-qwen3-api-key-here"}
export QWEN3_MODEL=${QWEN3_MODEL:-"qwen-turbo"}
export QWEN3_MAX_TOKENS=${QWEN3_MAX_TOKENS:-"2048"}
export QWEN3_TEMPERATURE=${QWEN3_TEMPERATURE:-"0.7"}

echo "Environment variables set:"
echo "  QWEN3_API_KEY: $QWEN3_API_KEY"
echo "  QWEN3_MODEL: $QWEN3_MODEL"
echo "  QWEN3_MAX_TOKENS: $QWEN3_MAX_TOKENS"
echo "  QWEN3_TEMPERATURE: $QWEN3_TEMPERATURE"

# Check if API key is set
if [ "$QWEN3_API_KEY" = "your-qwen3-api-key-here" ]; then
    echo "Warning: QWEN3_API_KEY is not set. Please set it before running the application."
    echo "You can set it by running: export QWEN3_API_KEY='your-actual-api-key'"
fi

# Build the application
echo "Building the application..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "Error: Build failed. Please check the Maven output above."
    exit 1
fi

echo "Build successful!"

# Run tests
echo "Running tests..."
mvn test -q

if [ $? -ne 0 ]; then
    echo "Warning: Some tests failed. Continuing anyway..."
fi

# Start the application
echo "Starting the application..."
echo "The application will be available at: http://localhost:8080"
echo "API endpoints will be available at: http://localhost:8080/api/qwen3/"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

# Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=qwen3