FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY backend/pom.xml ./
COPY backend/src ./src
COPY --from=frontend /app/dist ./src/main/resources/static
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend /app/target/meritcoin-api-1.0.0.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Xmx300m -XX:MaxMetaspaceSize=128m"
EXPOSE 8080
CMD ["sh", "-c", "test -n \"$ADMIN_PASSWORD\" && test -n \"$DATABASE_URL\" && test -n \"$DATABASE_USER\" && test -n \"$DATABASE_PASSWORD\" && test -n \"$BREVO_API_KEY\" && test -n \"$MAIL_FROM\" && exec java -jar app.jar"]
