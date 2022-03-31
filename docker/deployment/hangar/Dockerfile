FROM openjdk:16-jdk-alpine

LABEL maintainer="Yannick Lamprecht <yannicklamprecht@live.de>"

WORKDIR hangar

ADD target/hangar-*.jar /hangar/app.jar
ADD docker/deployment/hangar/application.yml /hangar/application.yml

ENV TERM xterm-256color

EXPOSE 8080
# "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" is for debug only
ENTRYPOINT ["java", "-XX:+ShowCodeDetailsInExceptionMessages", "-jar", "app.jar"]
