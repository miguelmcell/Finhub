#FROM openjdk:14
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]


FROM maven:3.6.3-jdk-14
COPY ./ ./
ENV spring.profiles.active=prod
RUN mvn clean package

CMD ["java", "-Dspring.profiles.active=prod","-jar", "target/robinhoodhub-0.0.1-SNAPSHOT.jar"]



