#!/bin/bash

echo "Testing Mock Server..."
echo "======================"

# Test 1: Check if server is running
echo "1. Checking server health..."
curl -s http://localhost:8080/admin/health | jq .

# Test 2: Add a test rule
echo -e "\n2. Adding a test rule..."
curl -s -X POST http://localhost:8080/admin/rules \
  -H "Content-Type: application/json" \
  -d '{
    "id": "",
    "name": "Test Rule",
    "method": "GET",
    "path": "/test",
    "statusCode": 200,
    "responseBody": "{\"message\": \"Hello from test rule!\"}",
    "delay": 0,
    "enabled": true,
    "priority": 0
  }' | jq .

# Test 3: Make a request to trigger recording
echo -e "\n3. Making a test request to /mock/test..."
curl -s http://localhost:8080/mock/test

# Test 4: Check recorded requests
echo -e "\n4. Checking recorded requests..."
curl -s http://localhost:8080/admin/requests | jq .

# Test 5: Check rules
echo -e "\n5. Checking rules..."
curl -s http://localhost:8080/admin/rules | jq .

echo -e "\nTest completed!" 