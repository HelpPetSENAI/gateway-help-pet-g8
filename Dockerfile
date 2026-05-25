# ============================================================
# Dockerfile - HelpPet Gateway
# Multi-stage build: compila e gera imagem minima
# ============================================================

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copia apenas os arquivos de dependencias primeiro (cache de camadas)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Baixa as dependencias (cache separado do codigo-fonte)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copia o codigo-fonte e compila
COPY src src
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime (imagem minima)
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Usuario nao-root por seguranca
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia apenas o JAR gerado
COPY --from=builder /app/target/*.jar app.jar

# Cria diretorio de logs com permissao correta
RUN mkdir -p logs && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Health check nativo do Docker
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
    "-jar", "app.jar"]
