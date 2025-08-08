# Use OpenJDK 17 base image
FROM openjdk:17-jdk

# Set working directory inside the container
WORKDIR /app

# Copy your exported JAR into the container
COPY Zyra_Server/out/artifacts/Zyra_Server_jar/Zyra_Server.jar app.jar

# Render uses a dynamic port assigned via $PORT env var — your app must respect it
ENV PORT=8080
EXPOSE 8080

# Start the Java app
CMD ["java", "-jar", "app.jar"]
