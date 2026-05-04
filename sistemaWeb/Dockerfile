# ===========================================
# PetyZoos Backend — Multi-stage Build
# Stage 1: Build con Maven
# Stage 2: Run con JRE 21
# ===========================================

# --- STAGE 1: BUILD ---
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar pom.xml primero (cache de dependencias)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y construir
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- STAGE 2: RUN ---
FROM eclipse-temurin:21-jre-alpine AS production

# Crear usuario no-root para seguridad
RUN addgroup -S petyzoos && adduser -S petyzoos -G petyzoos

WORKDIR /app

# Copiar JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Crear directorio de uploads
RUN mkdir -p uploads/veterinarios && chown -R petyzoos:petyzoos /app

# Cambiar a usuario no-root
USER petyzoos

# Puerto expuesto
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=40s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/api/auth/login || exit 1

# Variables de JVM para producción
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
