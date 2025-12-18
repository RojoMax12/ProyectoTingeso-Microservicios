FROM eclipse-temurin:17-jdk
ARG JAR_FILE=target/ReportyconsMicroservices.jar
COPY ${JAR_FILE} ReportyconsMicroservices.jar
ENTRYPOINT ["java", "-jar", "/ReportyconsMicroservices.jar"]