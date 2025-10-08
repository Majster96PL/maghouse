FROM maven:3.9.6-eclipse-temurin-22 AS builder
WORKDIR /maghouse
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:22-jre-alpine
WORKDIR /maghouse
COPY --from=builder /maghouse/target/*.jar maghouse2.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.datasource.username=${DB_USER}", "-Dspring.datasource.password=${DB_PASSWORD}"  , "-jar", "maghouse2.jar"]