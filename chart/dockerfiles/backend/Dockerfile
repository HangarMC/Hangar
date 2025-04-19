FROM bellsoft/liberica-openjdk-alpine:21

RUN addgroup -g 1000 java \
  && adduser -Ss /bin/false -u 1000 -G java -h /home/java java

USER java
WORKDIR hangar

ENV TERM xterm-256color

EXPOSE 8080
ENTRYPOINT ["sh", "run-java.sh"]

ENV JAVA_APP_JAR app.jar
ENV JAVA_APP_NAME hangar
ENV JAVA_ENABLE_DEBUG false

ADD --chown=java:java chart/dockerfiles/backend/run-java.sh /hangar/run-java.sh
ADD --chown=java:java backend/target/hangar-*.jar /hangar/app.jar
