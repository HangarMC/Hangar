FROM openjdk:14-jdk-alpine

LABEL maintainer="Yannick Lamprecht <yannicklamprecht@live.de>"

ADD target/hangar-*.jar app.jar
ADD docker/hangar/application.properties application.properties

ENV TERM xterm-256color

EXPOSE 8080
# "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" is for debug only
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom -Djava.net.preferIPv4Stack=true", "-jar", "app.jar",  "--spring.config.location=classpath:file:application.properties"]
