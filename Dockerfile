FROM openjdk:23-jdk-slim

RUN apt-get update && apt-get install -y \
    fontconfig \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/* \

LABEL authors="ivikto"
WORKDIR /app
COPY Production_Analyzer-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]