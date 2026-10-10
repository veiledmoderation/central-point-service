
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y --no-install-recommends gradle \
    && gradle bootJar --no-daemon \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

CMD ["sh", "-c", "java -jar build/libs/*.jar --server.port=${PORT:-8080}"]
