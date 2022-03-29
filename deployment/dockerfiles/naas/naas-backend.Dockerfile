FROM openjdk:17-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=naas-lib/build/libs/*.jar
COPY ${JAR_FILE} naas-lib-1.0.0.jar
EXPOSE 7272
ENTRYPOINT ["java","-jar","/naas-lib-1.0.0.jar"]