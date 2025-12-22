FROM eclipse-temurin:17-jdk
ARG JAR_FILE=target/Config.jar
COPY ${JAR_FILE} Config.jar
ENTRYPOINT ["java", "-jar", "/Config.jar"]