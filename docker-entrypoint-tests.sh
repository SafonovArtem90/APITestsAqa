#!/bin/sh
set -e

echo "Starting test application..."
java -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ -jar /app/resources/jars/internal-0.0.1-SNAPSHOT.jar &
APP_PID=$!

echo "Waiting for application to be ready on port 8080..."
for i in $(seq 1 60); do
    CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/endpoint \
        -H "X-Api-Key: qazWSXedc" \
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
