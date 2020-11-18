FROM openjdk:11.0.7-jdk-slim
ADD target/hspc-sandbox-manager-api-*.jar app.jar
ENV JAVA_OPTS=""
# install curl for local testing
RUN apt-get update && apt-get -y install curl
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]