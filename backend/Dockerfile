FROM openjdk:17-jdk-slim

WORKDIR /app/backend

COPY target/quash-0.0.1-SNAPSHOT.jar ./quash-backend.jar
COPY src/main/resources/application.properties ./application.properties

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar quash-backend.jar"]