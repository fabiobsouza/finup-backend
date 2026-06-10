# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/finup-api-*.jar app.jar

# Timezone
RUN apk add --no-cache tzdata
ENV TZ=America/Sao_Paulo

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
