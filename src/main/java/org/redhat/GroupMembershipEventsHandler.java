package org.redhat;

import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import sh.ory.keto.api.WriteApi;
import sh.ory.keto.model.InternalRelationTuple;

import java.util.Locale;

public class GroupMembershipEventsHandler extends CommonAdminEventsHandler implements AdminEventsHandler {

    protected GroupMembershipEventsHandler(KeycloakSession session, WriteApi writeApi) {
        super(session, writeApi);
    }

    @Override
    public void handleEvent(AdminEvent adminEvent, String[] parts) throws Exception {
        GroupModel membershipGroup = this.session.groups().getGroupById(session.getContext().getRealm(), parts[3]);
        UserModel membershipUser = this.session.users().getUserById(session.getContext().getRealm(), parts[1]);
        InternalRelationTuple internalRelationTuple = new InternalRelationTuple()
                .relation(MEMBER_RELATION)
                .namespace(WORKSPACES_NAMESPACE)
                ._object(membershipGroup.getName().toLowerCase(Locale.ROOT))
                .subject(buildSubject(USERS_NAMESPACE, membershipUser.getUsername().toLowerCase(Locale.ROOT)));
        if (adminEvent.getOperationType() == OperationType.CREATE) {
            System.out.println("Would delete: " + internalRelationTuple.toString());
            this.writeApi.createRelationTuple(internalRelationTuple);
        }
        if (adminEvent.getOperationType() == OperationType.DELETE) {
            System.out.printf("Would call delete with: Workspace: %s, Object: %s, Relation: %s and Subject: %s",
                    WORKSPACES_NAMESPACE, membershipGroup.getName().toLowerCase(Locale.ROOT), MEMBER_RELATION,
                    buildSubject(USERS_NAMESPACE, membershipUser.getUsername().toLowerCase(Locale.ROOT)));
            this.writeApi.deleteRelationTuple(WORKSPACES_NAMESPACE,
                    membershipGroup.getName().toLowerCase(Locale.ROOT), MEMBER_RELATION,
                    buildSubject(USERS_NAMESPACE, membershipUser.getUsername().toLowerCase(Locale.ROOT)));
        }

    }
}
