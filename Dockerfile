FROM maven:3.8.1-jdk-8-slim as builder

COPY . .

RUN mvn clean install

FROM quay.io/keycloak/keycloak:15.0.2

COPY --from=builder /target/*jar-with-dependencies.jar /opt/jboss/keycloak/standalone/deployments

EXPOSE 8080