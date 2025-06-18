#!/bin/bash

echo "🚀 Starting Mock Server..."
echo "=========================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java version $JAVA_VERSION detected. Please install Java 21 or higher."
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"

# Check if port 8080 is already in use
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  Port 8080 is already in use."
    echo "Do you want to kill the existing process? (y/n)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        echo "🔄 Killing existing process on port 8080..."
        lsof -ti:8080 | xargs kill -9
        sleep 2
    else
        echo "❌ Please free up port 8080 and try again."
        exit 1
    fi
fi

echo "🔄 Starting Mock Server on http://localhost:8080"
echo "📊 Dashboard: http://localhost:8080/index.html"
echo "🏥 Health check: http://localhost:8080/admin/health"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Start the application
./mvnw spring-boot:run 