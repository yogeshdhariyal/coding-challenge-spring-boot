FROM eclipse-temurin:21-jdk-jammy

COPY target/*.jar coding-challenge-spring-boot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "coding-challenge-spring-boot.jar"]