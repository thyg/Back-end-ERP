# Étape 1 : On utilise une image Maven pour construire le projet
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# On compile le projet en sautant les tests pour aller plus vite
RUN mvn clean package -DskipTests

# Étape 2 : On crée l'image finale légère pour lancer l'app
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# On récupère le fichier .jar créé à l'étape 1
COPY --from=build /app/target/*.jar app.jar
# On expose le port 8080
EXPOSE 8080
# La commande de démarrage
ENTRYPOINT ["java","-jar","app.jar"]