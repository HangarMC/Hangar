package io.papermc.hangar.model.common.roles;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import org.jetbrains.annotations.NotNull;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import java.util.Arrays;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrganizationRole implements Role<OrganizationRoleTable> {

    ORGANIZATION_SUPPORT("Organization_Support", 20, Permission.PostAsOrganization.add(Permission.IsOrganizationMember), "Support", Color.TRANSPARENT, 60),
    ORGANIZATION_EDITOR("Organization_Editor", 21, ProjectRole.PROJECT_EDITOR.getPermissions().add(ORGANIZATION_SUPPORT.permissions), "Editor", Color.TRANSPARENT, 50),
    ORGANIZATION_DEVELOPER("Organization_Developer", 22, ProjectRole.PROJECT_DEVELOPER.getPermissions().add(ORGANIZATION_EDITOR.permissions), "Developer", Color.TRANSPARENT, 40),
    ORGANIZATION_MAINTAINER("Organization_Maintainer", 23, Permission.CreateProject.add(Permission.EditProjectSettings).add(ProjectRole.PROJECT_MAINTAINER.getPermissions()).add(ORGANIZATION_DEVELOPER.permissions), "Maintainer", Color.TRANSPARENT, 30),
    ORGANIZATION_ADMIN("Organization_Admin", 24, Permission.IsOrganizationAdmin.add(ORGANIZATION_MAINTAINER.permissions).add(ProjectRole.PROJECT_ADMIN.getPermissions()), "Owner", Color.TRANSPARENT, 20),
    ORGANIZATION_OWNER("Organization_Owner", 25, Permission.IsOrganizationOwner.add(ProjectRole.PROJECT_OWNER.getPermissions()).add(ORGANIZATION_ADMIN.permissions), "Owner", Color.PURPLE, 10, false);

    private final String value;
    private final long roleId;
    private final Permission permissions;
    private final String title;
    private final Color color;
    private final boolean isAssignable;
    private final int rank;

    private static final OrganizationRole[] VALUES = values();
    private static final List<OrganizationRole> ASSIGNABLE_ROLES = Arrays.stream(VALUES).filter(OrganizationRole::isAssignable).toList();

    public static OrganizationRole[] getValues() {
        return VALUES;
    }

    public static List<OrganizationRole> getAssignableRoles() {
        return ASSIGNABLE_ROLES;
    }

    OrganizationRole(String value, long roleId, Permission permissions, String title, Color color, int rank) {
        this(value, roleId, permissions, title, color, rank, true);
    }

    OrganizationRole(String value, long roleId, Permission permissions, String title, Color color, int rank, boolean isAssignable) {
        this.value = value;
        this.roleId = roleId;
        this.permissions = permissions;
        this.title = title;
        this.color = color;
        this.rank = rank;
        this.isAssignable = isAssignable;
        Role.registerRole(this);
    }

    @NotNull
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    @NotNull
    @Override
    public RoleCategory getRoleCategory() {
        return RoleCategory.ORGANIZATION;
    }

    @NotNull
    @Override
    public Permission getPermissions() {
        return permissions;
    }

    @NotNull
    @Override
    public String getTitle() {
        return title;
    }

    @NotNull
    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public boolean isAssignable() {
        return isAssignable;
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    @Override
    public @NotNull OrganizationRoleTable create(Long organizationId, long userId, boolean isAccepted) {
        Preconditions.checkNotNull(organizationId, "organization id");
        return new OrganizationRoleTable(userId, this, isAccepted, organizationId);
    }
}
