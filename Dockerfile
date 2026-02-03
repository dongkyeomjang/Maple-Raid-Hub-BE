FROM eclipse-temurin:21-jre

WORKDIR /app

ARG JAR_PATH=./build/libs
COPY ${JAR_PATH}/app.jar ./app.jar

ENTRYPOINT ["java","-jar","./app.jar","--spring.profiles.active=prod"]
