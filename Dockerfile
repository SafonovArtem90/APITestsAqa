FROM eclipse-temurin:17-jre

WORKDIR /app

COPY resources/jars/internal-0.0.1-SNAPSHOT.jar app.jar

ENV SECRET=qazWSXedc
ENV MOCK_URL=http://localhost:8888/

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dsecret=${SECRET} -Dmock=${MOCK_URL} -jar app.jar"]
