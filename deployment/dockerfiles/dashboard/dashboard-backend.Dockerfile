FROM openjdk:14-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=dashboard-lib/build/libs/*.jar
COPY ${JAR_FILE} dashboard-lib-1.0.0.jar
EXPOSE 7173
ENTRYPOINT ["java","-jar","/dashboard-lib-1.0.0.jar"]