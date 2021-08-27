package org.redhat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.util.JsonSerialization;
import sh.ory.keto.api.WriteApi;
import sh.ory.keto.model.InternalRelationTuple;
import sh.ory.keto.model.PatchDelta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class GroupsEventsHandler extends CommonAdminEventsHandler implements AdminEventsHandler {

    public GroupsEventsHandler(KeycloakSession session, WriteApi writeApi) {
        super(session, writeApi);
    }

    @Override
    public void handleEvent(AdminEvent adminEvent, String[] parts) throws Exception {
        System.out.println("Action: " + adminEvent.getOperationType() +
                ", Type: " + parts[0] +
                ", Id: " + parts[1]);
        GroupRepresentation group = JsonSerialization.readValue(adminEvent.getRepresentation(), GroupRepresentation.class);
        System.out.println(group);
        //GroupModel group = super.session.groups().getGroupById(session.getContext().getRealm(), parts[1]);
        if (group == null) {
            return;
        }
        System.out.println("GroupRepresentation: " + group);
        if (adminEvent.getOperationType() == OperationType.CREATE) {
            System.out.println("Path: " + adminEvent.getResourcePath());
            List<PatchDelta> createDeltas = new ArrayList<>();
            if (adminEvent.getResourcePath().contains("/children")) {
                System.out.println("Creating subgroup");
                GroupRepresentation rp = new ObjectMapper().readValue(adminEvent.getRepresentation(), GroupRepresentation.class);
                Optional<GroupRepresentation> subgroup = group.getSubGroups().stream().filter(groupModel -> groupModel.getId().equals(rp.getId())).findFirst();
                if (subgroup.isPresent()) {
                    createDeltas.add(new PatchDelta().action(PATCH_ACTION_INSERT).relationTuple(
                            new InternalRelationTuple()
                                    .namespace(WORKSPACES_NAMESPACE)
                                    .relation(PARENT_RELATION)
                                    ._object(subgroup.get().getName().toLowerCase(Locale.ROOT))
                                    .subject(buildSubject(WORKSPACES_NAMESPACE, group.getName().toLowerCase(Locale.ROOT)))
                    ));
                    createDeltas.add(new PatchDelta().action(PATCH_ACTION_INSERT).relationTuple(
                            new InternalRelationTuple()
                                    .namespace(WORKSPACES_NAMESPACE)
                                    .relation(TAGS_RELATION)
                                    ._object(subgroup.get().getName().toLowerCase(Locale.ROOT))
                                    .subject(buildSubjectWithRelation(WORKSPACES_NAMESPACE, group.getName().toLowerCase(Locale.ROOT), TAGS_RELATION))
                    ));
                }

            } else {
                createDeltas.add(new PatchDelta().action(PATCH_ACTION_INSERT).relationTuple(
                        new InternalRelationTuple()
                                .namespace(WORKSPACES_NAMESPACE)
                                .relation(PARENT_RELATION)
                                ._object(group.getName().toLowerCase(Locale.ROOT))
                                .subject(buildSubject(ORGANIZATIONS_NAMESPACE,
                                        session.getContext().getRealm().getName().toLowerCase(Locale.ROOT)))
                ));
                createDeltas.add(new PatchDelta().action(PATCH_ACTION_INSERT).relationTuple(
                        new InternalRelationTuple()
                                .namespace(WORKSPACES_NAMESPACE)
                                .relation(TAGS_RELATION)
                                ._object(group.getName().toLowerCase(Locale.ROOT))
                                .subject(buildSubjectWithRelation(ORGANIZATIONS_NAMESPACE,
                                        session.getContext().getRealm().getName().toLowerCase(Locale.ROOT), TAGS_RELATION))
                ));
            }

            System.out.println(createDeltas);
            this.writeApi.patchRelationTuples(createDeltas);

        }
        if (adminEvent.getOperationType() == OperationType.UPDATE) {
            System.out.println(group.getAttributes()
                    .keySet()
                    .stream()
                    .map(key -> createTagsDeltaPatchWithAction(key, group.getName().toLowerCase(Locale.ROOT), PATCH_ACTION_DELETE))
                    .collect(Collectors.toList()));
            System.out.println(group.getAttributes()
                    .keySet()
                    .stream()
                    .map(key -> createTagsDeltaPatchWithAction(key, group.getName().toLowerCase(Locale.ROOT), PATCH_ACTION_INSERT))
                    .collect(Collectors.toList()));

            this.writeApi.patchRelationTuples(group.getAttributes()
                    .keySet()
                    .stream()
                    .map(key -> createTagsDeltaPatchWithAction(key, group.getName().toLowerCase(Locale.ROOT), PATCH_ACTION_DELETE))
                    .collect(Collectors.toList()));
            this.writeApi.patchRelationTuples(group.getAttributes()
                    .keySet()
                    .stream()
                    .map(key -> createTagsDeltaPatchWithAction(key, group.getName().toLowerCase(Locale.ROOT), PATCH_ACTION_INSERT))
                    .collect(Collectors.toList()));

        }
        if (adminEvent.getOperationType() == OperationType.DELETE) {

            this.writeApi.deleteRelationTuple(WORKSPACES_NAMESPACE,
                    group.getName().toLowerCase(Locale.ROOT), PARENT_RELATION,
                    buildSubject(ORGANIZATIONS_NAMESPACE, session.getContext().getRealm().getName().toLowerCase(Locale.ROOT)));
            this.writeApi.deleteRelationTuple(WORKSPACES_NAMESPACE,
                    group.getName().toLowerCase(Locale.ROOT), TAGS_RELATION,
                    buildSubjectWithRelation(ORGANIZATIONS_NAMESPACE, session.getContext().getRealm().getName().toLowerCase(Locale.ROOT), TAGS_RELATION));

        }
    }

    private PatchDelta createTagsDeltaPatchWithAction(String tagValue, String object, String action) {
        return new PatchDelta().action(action)
                .relationTuple(new InternalRelationTuple()
                        .namespace(WORKSPACES_NAMESPACE)
                        .relation(TAGS_RELATION)
                        ._object(object)
                        .subject(buildSubject(TAGS_NAMESPACE, tagValue)));
    }
}
