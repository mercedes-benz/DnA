#Step-1
FROM gradle:8.10.2-jdk21 AS TEMP_BUILD_IMAGE
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

#Step-2
FROM eclipse-temurin:21-jre-alpine
RUN apk --no-cache add curl
USER 1000
ENV ARTIFACT_NAME=lib-1.0.0.jar
COPY --from=TEMP_BUILD_IMAGE /home/gradle/src/lib/build/libs/$ARTIFACT_NAME $ARTIFACT_NAME

EXPOSE 9292
CMD java -jar $ARTIFACT_NAME
