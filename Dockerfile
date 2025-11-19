FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . /app
RUN ./gradlew bootJar -x test
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/build/libs/leum-server.jar"]