#!/bin/sh
set -e

if [ -z "$SECRET" ]; then
    echo "SECRET env var is required (passed via GitHub Actions secrets)."
    exit 1
fi
if [ -z "$SERVICE_API_KEY" ]; then
    echo "SERVICE_API_KEY env var is required (passed via GitHub Actions secrets)."
    exit 1
fi

MOCK_URL=${MOCK_URL:-http://localhost:8888/}

echo "Starting test application..."
java -Dsecret="$SECRET" -Dmock="$MOCK_URL" -jar /app/resources/jars/internal-0.0.1-SNAPSHOT.jar &
APP_PID=$!

echo "Waiting for application to be ready on port 8080..."
for i in $(seq 1 60); do
    CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/endpoint \
        -H "X-Api-Key: $SERVICE_API_KEY" \
        -d "token=AABBCCDDEEFF00112233445566778899&action=LOGIN" || true)
    if [ "$CODE" != "000" ]; then
        echo "Application is up (HTTP $CODE)."
        break
    fi
    if [ "$i" = "60" ]; then
        echo "Application failed to start in time."
        kill $APP_PID || true
        exit 1
    fi
    sleep 1
done

echo "Running mvn test..."
set +e
mvn test
EXIT_CODE=$?
set -e

kill $APP_PID || true
exit $EXIT_CODE
