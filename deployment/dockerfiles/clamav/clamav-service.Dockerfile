FROM openjdk:14-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=malwarescan-lib/build/libs/*.jar
COPY ${JAR_FILE} malwarescan-lib-1.0.0.jar
EXPOSE 7171
ENTRYPOINT ["java","-jar","/malwarescan-lib-1.0.0.jar"]