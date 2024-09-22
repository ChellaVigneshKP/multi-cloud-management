FROM eclipse-temurin:21-jre
LABEL authors="Chella Vignesh K P"
COPY target/auth-0.0.1-SNAPSHOT.jar auth-service.jar
ENTRYPOINT ["java", "-jar", "/auth-service.jar"]
EXPOSE 6060