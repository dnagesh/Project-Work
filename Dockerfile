ARG DOCKER_REGISTRY_BASE
FROM ${DOCKER_REGISTRY_BASE}service-common

ARG JAR_FILE
ADD ${JAR_FILE} /app.jar

CMD java $JAVA_OPTS -XX:+PrintGC -Djava.security.egd=file:/dev/./urandom -jar /app.jar
