FROM eclipse-temurin:17-jdk
ARG JAR_FILE=target/Gateway.jar
COPY ${JAR_FILE} Gateway.jar
ENTRYPOINT ["java", "-jar", "/Gateway.jar"]