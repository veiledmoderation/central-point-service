
FROM gradle:9.2.1-jdk21 AS build

WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/
EXPOSE 8080

CMD ["sh", "-c", "java -jar $(find /app -maxdepth 1 -name '*.jar' ! -name 'dont-use-this.jar' | head -n 1) --server.port=${PORT:-8080}"]
