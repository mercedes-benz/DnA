#Step-1
FROM gradle:7.4.1-jdk17 AS TEMP_BUILD_IMAGE
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

#Step-2
FROM openjdk:14-jdk-alpine
ENV ARTIFACT_NAME=airflow-backend-lib-1.0.0.jar
RUN apk add git
RUN addgroup -S 1000 && adduser -S 1000 -G 1000
USER 1000:1000
COPY --from=TEMP_BUILD_IMAGE /home/gradle/src/airflow-backend-lib/build/libs/$ARTIFACT_NAME $ARTIFACT_NAME
COPY --chown=1000:1000 ./airflow-git-init.sh /tmp
COPY --chown=1000:1000 ./git-askpass-helper.sh /tmp
USER root
RUN chmod -R 777 /tmp/git-askpass-helper.sh && chmod -R 777 /tmp/airflow-git-init.sh
USER 1000:1000

EXPOSE 7171
CMD java -jar $ARTIFACT_NAME