#!/bin/bash

# Qwen3 AI Integration API Test Script
# This script provides examples of how to test the API endpoints

BASE_URL="http://localhost:8080/api/qwen3"

echo "Qwen3 AI Integration API Test Script"
echo "===================================="
echo ""

# Function to make API calls
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    echo "Testing: $method $endpoint"
    echo "Data: $data"
    echo "Response:"
    
    if [ "$method" = "GET" ]; then
        curl -s -X GET "$BASE_URL$endpoint" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL$endpoint"
    else
        curl -s -X POST "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data" | jq '.' 2>/dev/null || curl -s -X POST "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data"
    fi
    
    echo ""
    echo "----------------------------------------"
    echo ""
}

# Test 1: Health Check
echo "1. Testing Health Check"
make_request "GET" "/health"

# Test 2: System Summary
echo "2. Testing System Summary"
make_request "GET" "/summary"

# Test 3: System Data (all types)
echo "3. Testing System Data Collection"
make_request "GET" "/system-data?type=all"

# Test 4: Performance Analysis
echo "4. Testing Performance Analysis"
make_request "GET" "/performance"

# Test 5: Network Analysis
echo "5. Testing Network Analysis"
make_request "GET" "/network"

# Test 6: JVM Analysis
echo "6. Testing JVM Analysis"
make_request "GET" "/jvm"

# Test 7: Basic Chat
echo "7. Testing Basic Chat"
make_request "POST" "/chat" '{
  "message": "Hello, how are you?",
  "sessionId": "test-session-1"
}'

# Test 8: System Information Analysis
echo "8. Testing System Information Analysis"
make_request "POST" "/system-info" '{
  "query": "Show me system information",
  "dataType": "all"
}'

# Test 9: Command Execution
echo "9. Testing Command Execution"
make_request "POST" "/execute-command" '{
  "command": "echo \"Hello from system command\""
}'

# Test 10: Performance Data Only
echo "10. Testing Performance Data"
make_request "GET" "/system-data?type=performance"

# Test 11: Network Data Only
echo "11. Testing Network Data"
make_request "GET" "/system-data?type=network"

# Test 12: JVM Data Only
echo "12. Testing JVM Data"
make_request "GET" "/system-data?type=jvm"

# Test 13: Chat with System Context
echo "13. Testing Chat with System Context"
make_request "POST" "/chat" '{
  "message": "Analyze the system performance and provide recommendations",
  "sessionId": "test-session-2",
  "systemContext": {
    "custom_data": "test_value",
    "analysis_type": "performance"
  }
}'

echo "All API tests completed!"
echo ""
echo "Note: Some requests may fail if the Qwen3 API key is not configured."
echo "To configure the API key, set the QWEN3_API_KEY environment variable:"
echo "export QWEN3_API_KEY='your-actual-api-key'"