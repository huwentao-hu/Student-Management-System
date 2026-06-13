FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN bash mvnw dependency:go-offline -DskipTests

COPY src src
RUN bash mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN useradd --system --create-home --uid 10001 appuser

WORKDIR /app
COPY --from=build /workspace/target/student-management-*.jar app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
