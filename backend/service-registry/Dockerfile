FROM eclipse-temurin:21-jre
LABEL authors="Chella Vignesh K P"
COPY target/service-registry-0.0.1-SNAPSHOT.jar service-discovery.jar
ENTRYPOINT ["java", "-jar", "/service-discovery.jar"]
EXPOSE 8761
