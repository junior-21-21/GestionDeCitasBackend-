# Etapa de construcción (Build stage)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero los pom.xml para descargar dependencias y cachear esta capa
COPY pom.xml .
COPY sistemaWeb/pom.xml sistemaWeb/

# Descargamos dependencias (opcional, ayuda al cache)
RUN mvn dependency:go-offline -B -f pom.xml

# Ahora copiamos el código fuente
COPY sistemaWeb/src sistemaWeb/src

# Construimos el proyecto (saltando las pruebas para acelerar)
RUN mvn clean package -DskipTests

# Etapa de ejecución (Run stage)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el JAR compilado de la etapa anterior
COPY --from=build /app/sistemaWeb/target/*.jar app.jar

# Aseguramos la existencia de la carpeta de uploads si se usa localmente
RUN mkdir -p /app/uploads

# Exponemos el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
