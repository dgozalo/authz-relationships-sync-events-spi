package org.redhat;

import org.keycloak.models.KeycloakSession;
import sh.ory.keto.api.WriteApi;

public abstract class CommonAdminEventsHandler {

    protected KeycloakSession session;
    protected WriteApi writeApi;

    protected CommonAdminEventsHandler(KeycloakSession session, WriteApi writeApi) {
        this.session = session;
        this.writeApi = writeApi;
    }

    protected static String ORGANIZATIONS_NAMESPACE = "organizations";
    protected static String WORKSPACES_NAMESPACE = "workspaces";
    protected static String TAGS_NAMESPACE = "tags";
    protected static String USERS_NAMESPACE = "users";

    protected static String PARENT_RELATION = "parent";
    protected static String MEMBER_RELATION = "member";
    protected static String TAGS_RELATION = "tags";
    protected static String CAN_CONNECT_TO_CLUSTER = "connect_to_cluster";

    protected static String PATCH_ACTION_INSERT = "insert";
    protected static String PATCH_ACTION_DELETE = "delete";

    protected String buildSubject(String workspace, String object) {
        return String.format("%s:%s", workspace, object);
    }

    protected String buildSubjectWithRelation(String workspace, String object, String relation) {
        return String.format("%s:%s#%s", workspace, object, relation);
    }
}
