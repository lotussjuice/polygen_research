# Usamos una imagen base válida y ligera de Java 21
FROM eclipse-temurin:21-jdk-alpine

# Directorio de trabajo
WORKDIR /app

# Copiamos el jar generado
COPY target/polygen-app.jar app.jar

# Exponemos el puerto
EXPOSE 1010

# Ejecutamos
ENTRYPOINT ["java", "-jar", "app.jar"]