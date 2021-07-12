#FROM adoptopenjdk/openjdk11:jdk-11.0.8_10-debian-slim
FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]