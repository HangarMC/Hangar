FROM openjdk:14-jdk-alpine

LABEL maintainer="Yannick Lamprecht <yannicklamprecht@live.de>"

ADD target/hangar-*.jar app.jar
ADD docker/hangar/application.yml application.yml

ENV TERM xterm-256color

EXPOSE 8080
# "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" is for debug only
ENTRYPOINT ["java", "-jar", "app.jar"]
