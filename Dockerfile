FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY config config
COPY hooks hooks
RUN chmod +x gradlew

COPY src/main src/main
RUN ./gradlew bootJar --no-daemon -x test -x detekt -x detektMain \
    && find build/libs -name '*.jar' ! -name '*-plain.jar' -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --uid 1001 spring
COPY --from=build /workspace/app.jar app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
