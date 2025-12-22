FROM eclipse-temurin:17-jdk
ARG JAR_FILE=target/Eureka.jar
COPY ${JAR_FILE} Eureka.jar
ENTRYPOINT ["java", "-jar", "/Eureka.jar"]