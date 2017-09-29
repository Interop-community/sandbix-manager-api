FROM openjdk:8-jdk-alpine
ADD target/hspc-sandbox-manager-api-*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]