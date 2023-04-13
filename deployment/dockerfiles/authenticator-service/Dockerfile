#Step-1
FROM gradle:7.4.1-jdk17 AS TEMP_BUILD_IMAGE
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon
#Step-2
FROM openjdk:17-jdk
USER 1000
ENV ARTIFACT_NAME=authenticator-lib-1.0.0.jar
COPY --from=TEMP_BUILD_IMAGE /home/gradle/src/authenticator-lib/build/libs/$ARTIFACT_NAME $ARTIFACT_NAME

EXPOSE 8383
CMD java -jar $ARTIFACT_NAME