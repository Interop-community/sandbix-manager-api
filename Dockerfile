FROM openjdk:11.0.7-jdk-slim
ADD target/hspc-sandbox-manager-api-*.jar app.jar
ENV JAVA_OPTS=""
# install curl for local testing
RUN apt update && apt install curl -y   #corrected the package manager for debian based
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]