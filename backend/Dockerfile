#FROM java:8-jdk
#LABEL team=box app=summer-hackathon-api
#ADD target/summer-hackathon-api-0.0.1-SNAPSHOT.jar $JAR_PATH
#ENTRYPOINT ["java","-jar","/target/summer-hackathon-api-0.0.1-SNAPSHOT.jar"]

FROM openjdk:11
MAINTAINER baeldung.com
COPY target/summer-hackathon-api-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]