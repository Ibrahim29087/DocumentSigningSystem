# ─────────────────────────────────────────────
# Place this file at:
#   DocumentSigningsystem/Dockerfile
# (same level as pom.xml)
# ─────────────────────────────────────────────

# ── Stage 1: Build ──────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy dependency manifest first so Docker can cache this layer.
# Re-downloads only when pom.xml changes, not on every code change.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source and build the jar (skip tests — CI already ran them)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run ─────────────────────────────
# Slim JRE-only image — much smaller than a full JDK
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built jar from stage 1
COPY --from=build /app/target/*.jar app.jar

# Render injects PORT env var — Spring must listen on it
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
