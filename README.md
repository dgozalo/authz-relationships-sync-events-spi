# Authz Relationships Synchronizer Keycloak EventsSPI

Implementation of a Keycloak EventsSPI to send Admin Events to Ory Keto.

## <span style="color:red">:: POC CODE - CRINGE POSSIBLE ::</span>

## Packaging

### Standalone packaging

Simply call

```shell script
mvn clean install
```
This will create an uber jar in:
`target/keto-sync-events-spi-jar-with-dependencies.jar`

### Packaged with Keycloak

With the previous method, you'll still need to copy the jar into the Keycloak deployment folder, but 
you can also prepackage it with Keycloak in Docker:

```shell script
docker build -t keycloak_with_listener .
```

This multistage Docker build will compile the application and move the jar into the deployment
folder within the Keycloak Docker container, so it's ready to go when run.
