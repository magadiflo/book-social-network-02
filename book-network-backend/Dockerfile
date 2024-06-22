FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY ./mvnw ./
COPY ./.mvn ./.mvn
COPY ./pom.xml ./

RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline

COPY ./src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS extraction
WORKDIR /app
COPY --from=build /build/target/*.jar ./app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-alpine AS run
WORKDIR /app
COPY --from=extraction /app/dependencies ./
COPY --from=extraction /app/spring-boot-loader ./
COPY --from=extraction /app/snapshot-dependencies ./
COPY --from=extraction /app/application ./

ARG PROFILE=dev

ENV SPRING_PROFILES_ACTIVE=${PROFILE}
ENV DB_URL=jdbc:postgresql://postgres:5432/db_book_social_network
ENV ACTIVATION_EMAIL_URL=http://localhost/auth/activate-account

EXPOSE 8080
CMD ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dspring.datasource.url=${DB_URL}", "-Dapplication.mailing.frontend.activation-url=${ACTIVATION_EMAIL_URL}", "org.springframework.boot.loader.launch.JarLauncher"]