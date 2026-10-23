# Dockerfile multi-stage : on compile avec Maven, puis on ne garde que le .jar
# dans une image légère. Trois étapes : build, test, et l'image finale (la dernière).

# ---------- Étape 1 : compilation ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# On copie d'abord le pom.xml seul : tant qu'il ne change pas, Docker garde
# les dépendances téléchargées en cache et le build est beaucoup plus rapide.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Étape 2 : tests (utilisée par docker-compose.test.yml) ----------
FROM build AS test
CMD ["mvn", "-B", "verify"]

# ---------- Étape 3 : image finale, celle qui tourne en production ----------
FROM eclipse-temurin:21-jre

# Ne jamais faire tourner une application en root dans un conteneur
RUN groupadd --system app && useradd --system --gid app --no-create-home app

WORKDIR /app
COPY --from=build /app/target/candidatures.jar app.jar

USER app
EXPOSE 8080

# MaxRAMPercentage : Java adapte sa mémoire à la limite du conteneur
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
