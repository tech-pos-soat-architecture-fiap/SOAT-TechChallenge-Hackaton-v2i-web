ARG BASE_IMAGE=21-jdk-jammy

FROM eclipse-temurin:${BASE_IMAGE} AS builder

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y maven

RUN mvn clean package -DskipTests=true -f pom.xml

FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

COPY --from=builder /app/target/*.jar /app/Hackaton.jar

EXPOSE 8000

CMD ["java", "-jar", "/app/Hackaton.jar"]