FROM openjdk:23-oracle AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN microdnf install maven
RUN mvn clean package -DskipTests

FROM openjdk:23-oracle
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]