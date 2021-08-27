package org.redhat;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import sh.ory.keto.ApiClient;

public class AuthzRelationshipsSynchronizerEventsListenerFactory implements EventListenerProviderFactory {

    private ApiClient ketoClient;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new AuthzRelationshipsSynchronizerEventsListener(keycloakSession, ketoClient);
    }

    @Override
    public void init(Config.Scope scope) {
        this.ketoClient = new ApiClient();
        this.ketoClient.setBasePath(String.format("http://%s:%s", "keto", "4467"));
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) { }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "AuthzRelationshipsSynchronizerEventsListener";
    }

}
