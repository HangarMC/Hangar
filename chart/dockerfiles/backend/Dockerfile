FROM bellsoft/liberica-runtime-container:jdk-24-cds-slim-musl

RUN addgroup -g 1000 java \
  && adduser -Ss /bin/false -u 1000 -G java -h /home/java java

USER java
WORKDIR hangar

ENV TERM=xterm-256color

EXPOSE 8080
# "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" is for debug only
ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]

ADD --chown=java:java backend/target/hangar-*.jar /hangar/app.jar
