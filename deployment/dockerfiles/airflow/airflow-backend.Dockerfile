FROM openjdk:14-jdk-alpine
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

#Multi Step process  - But  slow
#Step-1
# FROM gradle:6.2.2-jdk8 AS TEMP_BUILD_IMAGE
# COPY --chown=gradle:gradle . /home/gradle/src
# WORKDIR /home/gradle/src
# RUN gradle build --no-daemon 
#Step-2
# FROM openjdk:8-jdk-alpine
# ENV ARTIFACT_NAME=datambc-2.0.0.jar
# ENV APP_HOME=/usr/app/
# WORKDIR $APP_HOME
# COPY --from=TEMP_BUILD_IMAGE /home/gradle/src/build/libs/*.jar $APP_HOME/
# EXPOSE 7171
# CMD ["java","-jar",$ARTIFACT_NAME]