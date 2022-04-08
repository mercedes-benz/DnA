FROM openjdk:17-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=storage-lib/build/libs/*.jar
COPY ${JAR_FILE} storage-lib-1.0.0.jar
EXPOSE 7171
ENTRYPOINT ["java","-jar","/storage-lib-1.0.0.jar"]