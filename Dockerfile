# Use a imagem base do Maven mais recente para construir a aplicação
FROM maven:3.8.5-openjdk-23 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Use a imagem base do OpenJDK 23 para rodar a aplicação
FROM openjdk:23-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]