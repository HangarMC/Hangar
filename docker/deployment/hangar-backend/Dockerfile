FROM openjdk:17-jdk-alpine

WORKDIR hangar

ENV TERM xterm-256color

EXPOSE 8080
# "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" is for debug only
ENTRYPOINT ["java", "-XX:+ShowCodeDetailsInExceptionMessages", "-jar", "app.jar"]

ADD docker/deployment/hangar-backend/application.yml /hangar/application.yml
ADD target/hangar-*.jar /hangar/app.jar
