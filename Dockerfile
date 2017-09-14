FROM openjdk:8-jdk-alpine
ADD hspc-sandbox-manager-api-0.2.0-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]