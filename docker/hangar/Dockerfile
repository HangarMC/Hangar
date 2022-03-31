FROM openjdk:16-jdk-alpine

LABEL maintainer="Yannick Lamprecht <yannicklamprecht@live.de>"

RUN set -x && \
    addgroup -g 1000 appuser && \
    adduser -u 1000 -D -G appuser appuser && \
    mkdir /uploads && \
    chown appuser:appuser /uploads && \
    apk add yarn

ENV TERM xterm-256color
#
#ADD target/hangar-*.jar app.jar
#ADD docker/hangar/application.yml application.yml
#
#
#EXPOSE 8080
## "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" is for debug only
#ENTRYPOINT ["java", "-XX:+ShowCodeDetailsInExceptionMessages", "-jar", "app.jar"]
