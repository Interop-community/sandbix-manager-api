FROM openjdk:11.0.7-jdk-slim
ADD target/hspc-sandbox-manager-api-*.jar app.jar
ENV JAVA_OPTS=""
# install curl for local testing
RUN apk --no-cache add curl
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]