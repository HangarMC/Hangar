package io.papermc.hangar.model.common;

import java.util.List;

public final class MemberPermissions {

    private static final PermissionGroup VERSIONS = new PermissionGroup("versions", List.of(
        NamedPermission.CREATE_VERSION,
        NamedPermission.EDIT_VERSION,
        NamedPermission.DELETE_VERSION
    ));
    private static final PermissionGroup PEOPLE = new PermissionGroup("people", List.of(
        NamedPermission.MANAGE_SUBJECT_MEMBERS
    ));

    public static final List<PermissionGroup> PROJECT_GROUPS = List.of(
        new PermissionGroup("project", List.of(
            NamedPermission.EDIT_SUBJECT_SETTINGS,
            NamedPermission.EDIT_PAGE,
            NamedPermission.EDIT_CHANNEL,
            NamedPermission.DELETE_PROJECT
        )),
        VERSIONS,
        PEOPLE
    );

    // delete_organization stays with the owner and cannot be handed out
    public static final List<PermissionGroup> ORGANIZATION_GROUPS = List.of(
        new PermissionGroup("organization", List.of(
            NamedPermission.EDIT_SUBJECT_SETTINGS
        )),
        new PermissionGroup("projects", List.of(
            NamedPermission.CREATE_PROJECT,
            NamedPermission.EDIT_PAGE,
            NamedPermission.EDIT_CHANNEL,
            NamedPermission.DELETE_PROJECT
        )),
        VERSIONS,
        PEOPLE
    );

    public static final Permission PROJECT_BASE = Permission.IsProjectMember;
    public static final Permission ORGANIZATION_BASE = Permission.IsOrganizationMember;

    public static final Permission PROJECT_ASSIGNABLE = orOf(PROJECT_GROUPS);
    public static final Permission ORGANIZATION_ASSIGNABLE = orOf(ORGANIZATION_GROUPS);

    public static final Permission PROJECT_OWNER = Permission.IsProjectOwner.add(PROJECT_ASSIGNABLE).add(PROJECT_BASE);
    public static final Permission ORGANIZATION_OWNER = Permission.IsOrganizationOwner.add(Permission.IsProjectOwner).add(ORGANIZATION_ASSIGNABLE).add(ORGANIZATION_BASE);

    public static final String DEFAULT_OWNER_TITLE = "Owner";
    public static final int MAX_TITLE_LENGTH = 32;

    private MemberPermissions() {
    }

    public static List<PermissionGroup> groups(final boolean organization) {
        return organization ? ORGANIZATION_GROUPS : PROJECT_GROUPS;
    }

    public static Permission base(final boolean organization) {
        return organization ? ORGANIZATION_BASE : PROJECT_BASE;
    }

    public static Permission assignable(final boolean organization) {
        return organization ? ORGANIZATION_ASSIGNABLE : PROJECT_ASSIGNABLE;
    }

    public static Permission owner(final boolean organization) {
        return organization ? ORGANIZATION_OWNER : PROJECT_OWNER;
    }

    private static Permission orOf(final List<PermissionGroup> groups) {
        return groups.stream().map(PermissionGroup::asPermission).reduce(Permission::add).orElse(Permission.None);
    }
}
