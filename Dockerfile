FROM openjdk:8-jdk-alpine

ENV DEBIAN_FRONTEND noninteractive

VOLUME /tmp

ADD target/hspc-sandbox-manager-api-*.jar app.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
