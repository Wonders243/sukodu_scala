FROM eclipse-temurin:21-jdk

# JavaFX dependencies
RUN apt-get update && apt-get install -y \
    libgtk-3-0 \
    libgl1 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/scala-2.13/*assembly*.jar app.jar

CMD ["java", "-jar", "app.jar"]