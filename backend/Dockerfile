FROM openjdk:21-jdk-slim as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x ./mvnw

RUN ./mvnw package -DskipTests

FROM openjdk:21-jdk-slim
VOLUME /tmp

RUN mkdir -p /app/uploads
WORKDIR /app

COPY --from=build /workspace/app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE 8080