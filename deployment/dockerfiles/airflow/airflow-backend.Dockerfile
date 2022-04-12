FROM openjdk:17-jdk-alpine
RUN apk add git
RUN addgroup -S 1000 && adduser -S 1000 -G 1000
#RUN mkdir -m 777 /git
#RUN chown -R spring:spring git && chmod -R 777 /git
USER 1000:1000

ARG JAR_FILE=airflow-backend-lib/build/libs/*.jar
COPY ${JAR_FILE} airflow-backend-lib-1.0.0.jar
COPY --chown=1000:1000 ./airflow-git-init.sh /tmp
COPY --chown=1000:1000 ./git-askpass-helper.sh /tmp
USER root
RUN chmod -R 777 /tmp/git-askpass-helper.sh && chmod -R 777 /tmp/airflow-git-init.sh
USER 1000:1000

EXPOSE 7171
ENTRYPOINT ["java","-jar","/airflow-backend-lib-1.0.0.jar"]


#Step-1
FROM gradle:7.4.1-jdk17 AS TEMP_BUILD_IMAGE
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon
#Step-2
FROM openjdk:14-jdk
ENV ARTIFACT_NAME=airflow-backend-lib/build/libs/*.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE /home/gradle/src/naas-lib/build/libs/$ARTIFACT_NAME $ARTIFACT_NAME

EXPOSE 7272
CMD java -jar $ARTIFACT_NAME