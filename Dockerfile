# ---------- Stage 1: Build ----------
FROM maven:3.9.10-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy the root pom and all module poms first
COPY pom.xml ./
COPY api/pom.xml api/pom.xml
COPY application/pom.xml application/pom.xml
COPY domain/pom.xml domain/pom.xml
COPY infrastructure-jpa/pom.xml infrastructure-jpa/pom.xml
COPY infrastructure-security/pom.xml infrastructure-security/pom.xml
COPY infrastructure-websocket/pom.xml infrastructure-websocket/pom.xml

RUN mvn -B -q -DskipTests dependency:go-offline

# Now copy the full source code
COPY . .

# Build only the api module and its dependencies
RUN mvn -B -DskipTests -pl api -am package

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/api/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]
