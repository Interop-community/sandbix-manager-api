FROM openjdk:11.0.7-jdk-slim

# mapped to host for filesystem mounting
VOLUME /tmp

ADD target/hspc-sandbox-manager-api*.jar app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
RUN mkdir -p /KeyPair
CMD ["ls", "/KeyPair"]

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]
