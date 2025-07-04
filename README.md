# Mock Server - Beeceptor-like Functionality

A Spring Boot application that implements Beeceptor-like functionality for mocking HTTP APIs.

## Features

- **Mock Endpoints**: Accept any HTTP request and return configurable responses
- **Rule-based Responses**: Define custom rules for specific paths and methods
- **Request Recording**: All requests are automatically recorded with full details
- **Persistent Storage**: Rules and requests are saved to JSON files
- **Web Dashboard**: User-friendly interface for managing rules and viewing requests
- **Copy Request Body**: Copy any recorded request body to clipboard with one click
- **Create Rule from Request**: Quickly create rules based on recorded requests
- **Individual Request Management**: Delete specific requests as needed
- **Request Delay**: Simulate network latency with configurable delays

## Quick Start

### 1. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 2. Access the Dashboard

Open your browser and go to: `http://localhost:8080/index.html`

### 3. Test the Application

Run the test script to verify everything is working:

```bash
./test-mock-server.sh
```

### 4. Stop the Application

There are several ways to stop the application:

#### Graceful Shutdown (Recommended)
Press `Ctrl+C` in the terminal where the application is running. This allows the application to shut down gracefully.

#### Force Kill Process on Port 8080
If the application is running in the background or you can't access the terminal:

```bash
lsof -ti:8080 | xargs kill -9
```

#### Using the Startup Script
If you used the provided startup script:

```bash
./start.sh stop
```

## API Endpoints

### Mock Endpoint
- **URL**: `/mock/**`
- **Methods**: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE
- **Description**: All requests to this endpoint are recorded and processed according to configured rules

### Admin Endpoints

#### Rules Management
- `GET /admin/rules` - Get all rules
- `POST /admin/rules` - Add a new rule
- `DELETE /admin/rules/{ruleId}` - Delete a specific rule
- `DELETE /admin/rules` - Clear all rules

#### Request Recording
- `GET /admin/requests` - Get all recorded requests
- `GET /admin/requests/{requestId}` - Get a specific recorded request
- `DELETE /admin/requests` - Clear all recorded requests

#### Health Check
- `GET /admin/health` - Get server status and statistics

## Rule Configuration

Rules define how the mock server should respond to specific requests.

### Rule Properties

```json
{
  "id": "rule_1",
  "name": "User API Response",
  "method": "GET",
  "path": "/api/users/*",
  "statusCode": 200,
  "responseBody": "{\"users\": [{\"id\": 1, \"name\": \"John\"}]}",
  "responseHeaders": {
    "Content-Type": "application/json",
    "X-Custom-Header": "value"
  },
  "delay": 1000,
  "enabled": true,
  "priority": 0
}
```

- **method**: HTTP method (use "*" for any method)
- **path**: URL path pattern (supports regex or exact match, use "*" for any path)
- **statusCode**: HTTP status code to return
- **responseBody**: Response body content
- **responseHeaders**: Custom headers to include in response
- **delay**: Response delay in milliseconds
- **enabled**: Whether the rule is active
- **priority**: Rule priority (higher numbers take precedence)

## Request Recording

All requests to `/mock/**` are automatically recorded and saved to the `requests` directory with the following information:

- Request method and path
- Query parameters
- Headers
- Request body
- Timestamp
- Remote address
- User agent

Files are saved with timestamp-based naming: `YYYYMMDD_HHMMSS_SSS_METHOD_PATH.json`

## Exposing to External Internet

To expose your mock server to the internet (like Beeceptor), you can use LocalTunnel:

### Using LocalTunnel

```bash
# Install LocalTunnel globally
npm install -g localtunnel

# Expose your local server
lt --port 8080 --subdomain yoursubdomain
```

Your mock server will be available at: `https://yoursubdomain.loca.lt`

### Using ngrok

```bash
# Install ngrok
# Download from https://ngrok.com/

# Expose your local server
ngrok http 8080
```

## Configuration

The application can be configured via `application.yml`:

```yaml
server:
  port: 8080

mock-server:
  recording:
    directory: requests  # Directory to save recorded requests
  cors:
    enabled: true
    allowed-origins: "*"
    allowed-methods: "*"
    allowed-headers: "*"
```

## Example Usage

### 1. Add a Rule via API

```bash
curl -X POST http://localhost:8080/admin/rules \
  -H "Content-Type: application/json" \
  -d '{
    "name": "JSON API Response",
    "method": "POST",
    "path": "/api/data",
    "statusCode": 201,
    "responseBody": "{\"id\": 123, \"status\": \"created\"}",
    "responseHeaders": {
      "Content-Type": "application/json"
    }
  }'
```

### 2. Make a Request

```bash
curl -X POST http://localhost:8080/mock/api/data \
  -H "Content-Type: application/json" \
  -d '{"name": "test"}'
```

### 3. View Recorded Requests

```bash
curl http://localhost:8080/admin/requests
```

## Troubleshooting

### Requests Not Being Recorded

1. Check the application logs for any errors
2. Verify the `requests` directory exists and is writable
3. Ensure requests are being made to `/mock/**` endpoints

### Rules Not Working

1. Check rule configuration (method, path, enabled status)
2. Verify rule priority settings
3. Check application logs for rule matching information

### Web Dashboard Not Loading

1. Ensure the application is running on port 8080
2. Check browser console for JavaScript errors
3. Verify CORS settings if accessing from a different domain

## Development

### Building the Application

```bash
./mvnw clean package
```

### Running Tests

```bash
./mvnw test
```

## License

This project is licensed under the MIT License.

### Web Dashboard

Access the web interface at `http://localhost:8080` to:
- View server status and statistics
- Add and manage response rules
- View all recorded requests
- Copy request bodies to clipboard
- Delete individual requests

### Copy Request Body Feature

The web dashboard includes a powerful copy functionality for recorded requests:

#### How to Use:
1. Open the web interface at `http://localhost:8080`
2. Navigate to the "Recorded Requests" section
3. Click the green **"Copy"** button next to any request
4. The request body will be copied to your clipboard

#### Features:
- **Smart Clipboard**: Uses modern clipboard API with fallback for older browsers
- **Visual Feedback**: Button shows different states:
  - `Copying...` - Processing the request
  - `Copied!` - Successfully copied to clipboard
  - `Show Body` - Fallback when clipboard fails (shows alert)
  - `Error` - If request fetch fails
- **Universal Support**: Works with any content type (JSON, XML, text, etc.)
- **Error Handling**: Graceful fallback if clipboard access is restricted

### Create Rule from Request Feature

Quickly create response rules based on recorded requests:

#### How to Use:
1. Open the web interface at `http://localhost:8080`
2. Navigate to the "Recorded Requests" section
3. Click the blue **"Create Rule"** button next to any request
4. The rule form will be automatically pre-populated with:
   - **Rule Name**: "Rule for [METHOD] [PATH]"
   - **HTTP Method**: The request method (GET, POST, PUT, DELETE, etc.)
   - **Path Pattern**: The request path without `/mock` prefix
   - **Status Code**: 200 (default)
   - **Response Body**: Sample response based on request content
   - **Delay**: 0ms (default)
5. Review and modify the rule as needed
6. Click "Add Rule" to create the rule

#### Features:
- **Automatic Form Population**: All fields pre-filled based on the request
- **Smart Path Handling**: Automatically strips `/mock` prefix for rule matching
- **Intelligent Response Templates**: 
  - JSON requests → JSON response template
  - XML requests → Text response template
  - GET requests → JSON response template
  - Complex data → Structured response template
- **Visual Feedback**: Form scrolls into view and highlights briefly
- **Quick Rule Creation**: Create rules in seconds instead of minutes

#### Example Workflow:
1. **Record a request**: `POST /mock/api/users` with JSON body
2. **Click "Create Rule"**: Form pre-populates with POST method and `/api/users` path
3. **Modify response**: Edit the sample response body as needed
4. **Add rule**: Click "Add Rule" to save
5. **Test**: Future requests to `/mock/api/users` will use your custom response

#### Technical Details (Copy Functionality):
- **Modern API**: Uses `navigator.clipboard.writeText()` when available
- **Fallback Method**: Uses `document.execCommand('copy')` for older browsers
- **Secure Context**: Works on localhost and HTTPS sites
- **Manual Fallback**: Shows request body in alert if all clipboard methods fail

#### Example Usage:
```bash
# Make a test request
curl -X POST http://localhost:8080/mock/api/test \
  -H "Content-Type: application/json" \
  -d '{"user": "john", "action": "login"}'

# The request will be recorded and you can copy its body via the web interface
``` 