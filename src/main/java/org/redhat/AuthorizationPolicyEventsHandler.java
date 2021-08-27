package org.redhat;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;
import sh.ory.keto.api.WriteApi;
import sh.ory.keto.model.InternalRelationTuple;
import sh.ory.keto.model.PatchDelta;

import java.util.*;

public class AuthorizationPolicyEventsHandler extends CommonAdminEventsHandler implements AdminEventsHandler {

    public AuthorizationPolicyEventsHandler(KeycloakSession session, WriteApi writeApi) {
        super(session, writeApi);
    }

    @Override
    public void handleEvent(AdminEvent adminEvent, String[] parts) throws Exception {
        if (!adminEvent.getResourcePath().contains("/authz/resource-server/permission/resource/")) {
            return;
        }
        switch (adminEvent.getOperationType()) {
            case UPDATE:
            case CREATE:
                AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
                ResourceServer resourceServer = authorization.getStoreFactory().getResourceServerStore().findById(this.session.getContext().getClient().getId());
                Policy policy = authorization.getStoreFactory().getPolicyStore().findById(parts[6], resourceServer.getId());

                Policy subPolicy = policy.getAssociatedPolicies().stream().findFirst().get();
                System.out.println(subPolicy.getConfig().get("groups"));
                List<LinkedHashMap<String, String>> groupList = JsonSerialization.readValue(subPolicy.getConfig().get("groups"), List.class);
                Resource resource = policy.getResources().stream().findFirst().get();

                GroupModel gr = this.session.groups().getGroupById(this.session.getContext().getRealm(), groupList.get(0).get("id"));
                List<PatchDelta> deltas = new ArrayList<>();
                deltas.add(new PatchDelta().action(PATCH_ACTION_INSERT).relationTuple(new InternalRelationTuple()
                        .namespace(resource.getType())
                        ._object(resource.getName())
                        .relation(PARENT_RELATION)
                        .subject(buildSubject(WORKSPACES_NAMESPACE, gr.getName().toLowerCase(Locale.ROOT)))));

                deltas.add(new PatchDelta().action(PATCH_ACTION_INSERT).relationTuple(new InternalRelationTuple()
                        .namespace(resource.getType())
                        ._object(resource.getName())
                        .relation(TAGS_RELATION)
                        .subject(buildSubjectWithRelation(WORKSPACES_NAMESPACE, gr.getName().toLowerCase(Locale.ROOT), TAGS_RELATION))));

                deltas.add(new PatchDelta().action(PATCH_ACTION_INSERT).relationTuple(new InternalRelationTuple()
                        .namespace(resource.getType())
                        ._object(resource.getName())
                        .relation(CAN_CONNECT_TO_CLUSTER)
                        .subject(buildSubjectWithRelation(WORKSPACES_NAMESPACE, gr.getName().toLowerCase(Locale.ROOT), MEMBER_RELATION))));
                writeApi.patchRelationTuples(deltas);
                break;
            default:
                System.out.println("No action");
        }
    }
}
