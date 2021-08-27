package org.redhat;

import org.keycloak.events.admin.AdminEvent;

import java.io.IOException;

public interface AdminEventsHandler {

    void handleEvent(AdminEvent adminEvent, String[] parts) throws Exception;

}
