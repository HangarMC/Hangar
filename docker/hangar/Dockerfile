FROM maven:3.6.3-openjdk-14
COPY ./pom.xml pom.xml
COPY ./src/ src/
RUN mvn clean package -DskipTests=true


FROM openjdk:14-jdk-alpine

LABEL maintainer="Yannick Lamprecht <yannicklamprecht@live.de>"

COPY --from=0 ./target/hangar-*.jar app.jar
ADD docker/hangar/application.properties application.properties

ENV TERM xterm-256color

EXPOSE 8080
# "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" is for debug only
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom -Djava.net.preferIPv4Stack=true", "-jar", "app.jar",  "--spring.config.location=file:application.properties"]
