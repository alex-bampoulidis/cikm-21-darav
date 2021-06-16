FROM openjdk:15-jdk-alpine
EXPOSE 4201
ARG JAR_FILE=target/privacy-backend-1.0.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]