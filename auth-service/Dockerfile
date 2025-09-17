FROM maven:3.9.9-eclipse-temurin-24 AS builder
LABEL authors="Maximilian Wey"

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:24-jdk-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE ${AUTH_SERVER_PORT:-8000}

ENTRYPOINT ["java","-jar","/app/app.jar"]