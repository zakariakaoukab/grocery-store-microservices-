# Stage 1: Build (uses pre-built JAR if available, otherwise compiles via Maven)
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common-library/pom.xml common-library/
COPY config-server/pom.xml config-server/
COPY discovery-server/pom.xml discovery-server/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY product-service/pom.xml product-service/
COPY supplier-service/pom.xml supplier-service/
COPY order-service/pom.xml order-service/

COPY api-gateway api-gateway

RUN if ! ls api-gateway/target/*-SNAPSHOT.jar 1>/dev/null 2>&1; then \
        mvn clean package -pl api-gateway -am -DskipTests; \
    fi

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/api-gateway/target/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
