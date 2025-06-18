#!/bin/bash

echo "🧪 Complete Mock Server Test"
echo "============================"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test 1: Health check
echo -e "\n${BLUE}1. Health Check${NC}"
HEALTH=$(curl -s http://localhost:8080/admin/health)
echo "Health: $HEALTH"

# Test 2: Add multiple rules
echo -e "\n${BLUE}2. Adding Rules${NC}"

# Rule 1: Simple GET rule
echo "Adding GET /test rule..."
curl -s -X POST http://localhost:8080/admin/rules \
  -H "Content-Type: application/json" \
  -d '{"name": "Test GET", "method": "GET", "path": "/test", "statusCode": 200, "responseBody": "Hello from GET test!"}'

# Rule 2: JSON API rule
echo -e "\nAdding POST /api/users rule..."
curl -s -X POST http://localhost:8080/admin/rules \
  -H "Content-Type: application/json" \
  -d '{"name": "Users API", "method": "POST", "path": "/api/users", "statusCode": 201, "responseBody": "{\"id\": 123, \"status\": \"created\"}", "responseHeaders": {"Content-Type": "application/json"}}'

# Rule 3: Wildcard rule
echo -e "\nAdding wildcard rule..."
curl -s -X POST http://localhost:8080/admin/rules \
  -H "Content-Type: application/json" \
  -d '{"name": "Wildcard", "method": "*", "path": "/wildcard/*", "statusCode": 200, "responseBody": "Wildcard response!"}'

# Test 3: List rules
echo -e "\n${BLUE}3. Current Rules${NC}"
curl -s http://localhost:8080/admin/rules | jq .

# Test 4: Test rule matching
echo -e "\n${BLUE}4. Testing Rule Matching${NC}"

echo "Testing GET /mock/test (should match rule):"
curl -s http://localhost:8080/mock/test
echo -e "\n"

echo "Testing POST /mock/api/users (should match rule):"
curl -s -X POST http://localhost:8080/mock/api/users -H "Content-Type: application/json" -d '{"name": "test"}'
echo -e "\n"

echo "Testing GET /mock/wildcard/anything (should match wildcard):"
curl -s http://localhost:8080/mock/wildcard/anything
echo -e "\n"

echo "Testing GET /mock/nonexistent (should return default):"
curl -s http://localhost:8080/mock/nonexistent
echo -e "\n"

# Test 5: Check recorded requests
echo -e "\n${BLUE}5. Recorded Requests${NC}"
REQUESTS=$(curl -s http://localhost:8080/admin/requests | jq 'length')
echo "Total recorded requests: $REQUESTS"
curl -s http://localhost:8080/admin/requests | jq '.[0:3] | .[] | {id, method, path, timestamp}'

# Test 6: Check persistence
echo -e "\n${BLUE}6. Persistence Check${NC}"
echo "Rules file exists: $(test -f rules/rules.json && echo 'YES' || echo 'NO')"
echo "Requests file exists: $(test -f requests/requests.json && echo 'YES' || echo 'NO')"
echo "Individual request files: $(ls requests/*.json | grep -v requests.json | wc -l | tr -d ' ')"

# Test 7: Test web UI
echo -e "\n${BLUE}7. Web UI Check${NC}"
UI_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/index.html)
if [ "$UI_STATUS" = "200" ]; then
    echo -e "${GREEN}✅ Web UI is accessible at http://localhost:8080/index.html${NC}"
else
    echo -e "${RED}❌ Web UI returned status: $UI_STATUS${NC}"
fi

# Test 8: Final health check
echo -e "\n${BLUE}8. Final Health Check${NC}"
FINAL_HEALTH=$(curl -s http://localhost:8080/admin/health)
echo "Final health: $FINAL_HEALTH"

echo -e "\n${GREEN}✅ All tests completed!${NC}"
echo -e "\n${BLUE}Next steps:${NC}"
echo "1. Open http://localhost:8080/index.html in your browser"
echo "2. Click 'Refresh Rules' and 'Refresh Requests' to see the data"
echo "3. Add new rules through the web interface"
echo "4. Test with: curl http://localhost:8080/mock/your-path" 