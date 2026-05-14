FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY skill-gateway/target/skill-gateway-1.0.2.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
