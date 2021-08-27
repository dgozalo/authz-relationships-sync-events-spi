package org.redhat;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import sh.ory.keto.ApiClient;
import sh.ory.keto.api.WriteApi;


public class AuthzRelationshipsSynchronizerEventsListener implements EventListenerProvider {

    private KeycloakSession session;
    private WriteApi writeApi;

    public AuthzRelationshipsSynchronizerEventsListener(KeycloakSession keycloakSession, ApiClient ketoClient) {
        this.session = keycloakSession;
        this.writeApi = new WriteApi(ketoClient);
    }

    @Override
    public void onEvent(Event event) {
        System.out.printf("Received Event: %s%n", eventToString(event));
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        System.out.printf("Received Admin Event: %s%n", eventToString(adminEvent));
        System.out.printf("Representation: %s%n", adminEvent.getRepresentation());
        try {
            String[] parts = adminEvent.getResourcePath().split("/");
            switch (adminEvent.getResourceType()) {
                case GROUP:
                    new GroupsEventsHandler(session, writeApi).handleEvent(adminEvent, parts);
                    break;
                case GROUP_MEMBERSHIP:
                    new GroupMembershipEventsHandler(session, writeApi).handleEvent(adminEvent, parts);
                    break;
                case AUTHORIZATION_POLICY:
                    new AuthorizationPolicyEventsHandler(session, writeApi).handleEvent(adminEvent, parts);
                    break;
                default:
                    System.out.println("Nothing to do");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {

    }

    private String eventToString(AdminEvent adminEvent) {
        StringBuilder sb = new StringBuilder();
        sb.append("resourceType=");
        sb.append(adminEvent.getResourceTypeAsString());
        sb.append(", operationType=");
        sb.append(adminEvent.getOperationType());
        sb.append(", realmId=");
        sb.append(adminEvent.getAuthDetails().getRealmId());
        sb.append(", clientId=");
        sb.append(adminEvent.getAuthDetails().getClientId());
        sb.append(", userId=");
        sb.append(adminEvent.getAuthDetails().getUserId());
        sb.append(", ipAddress=");
        sb.append(adminEvent.getAuthDetails().getIpAddress());
        sb.append(", resourcePath=");
        sb.append(adminEvent.getResourcePath());
        if (adminEvent.getError() != null) {
            sb.append(", error=");
            sb.append(adminEvent.getError());
        }
        return sb.toString();
    }

    private String eventToString(Event event) {
        StringBuilder sb = new StringBuilder();
        sb.append("type=");
        sb.append(event.getType());
        sb.append(", realmId=");
        sb.append(event.getRealmId());
        sb.append(", clientId=");
        sb.append(event.getClientId());
        sb.append(", userId=");
        sb.append(event.getUserId());
        sb.append(", ipAddress=");
        sb.append(event.getIpAddress());
        if (event.getError() != null) {
            sb.append(", error=");
            sb.append(event.getError());
        }
        if (event.getDetails() != null) {
            event.getDetails().entrySet().stream().map(e -> {
                StringBuilder eventDetailsBuilder = new StringBuilder();
                eventDetailsBuilder.append(", ");
                eventDetailsBuilder.append(e.getKey());
                if (e.getValue() == null || e.getValue().indexOf(' ') == -1) {
                    eventDetailsBuilder.append("=");
                    eventDetailsBuilder.append(e.getValue());
                } else {
                    eventDetailsBuilder.append("='");
                    eventDetailsBuilder.append(e.getValue());
                    eventDetailsBuilder.append("'");
                }
                return eventDetailsBuilder.toString();
            });
        }
        return sb.toString();
    }
}
