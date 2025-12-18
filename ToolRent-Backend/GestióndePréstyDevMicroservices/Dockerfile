FROM eclipse-temurin:17-jdk
ARG JAR_FILE=target/GestióndePréstyDevMicroservices.jar
COPY ${JAR_FILE} GestióndePréstyDevMicroservices.jar
ENTRYPOINT ["java", "-jar", "/GestióndePréstyDevMicroservices.jar"]