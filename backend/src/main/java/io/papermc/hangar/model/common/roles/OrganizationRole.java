package io.papermc.hangar.model.common.roles;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrganizationRole implements Role<OrganizationRoleTable> {

    ORGANIZATION_SUPPORT("Organization_Support", 20, Permission.PostAsOrganization.add(Permission.IsOrganizationMember), "Support", Color.AMBER, 60),
    ORGANIZATION_EDITOR("Organization_Editor", 21, ProjectRole.PROJECT_EDITOR.getPermissions().add(ORGANIZATION_SUPPORT.permissions), "Editor", Color.AMBER, 50),
    ORGANIZATION_DEVELOPER("Organization_Developer", 22, ProjectRole.PROJECT_DEVELOPER.getPermissions().add(ORGANIZATION_EDITOR.permissions), "Developer", Color.ORANGE, 40),
    ORGANIZATION_MAINTAINER("Organization_Maintainer", 23, Permission.CreateProject.add(Permission.EditProjectSettings).add(ProjectRole.PROJECT_MAINTAINER.getPermissions()).add(ORGANIZATION_DEVELOPER.permissions), "Maintainer", Color.AQUA, 30),
    ORGANIZATION_ADMIN("Organization_Admin", 24, Permission.IsOrganizationAdmin.add(ORGANIZATION_MAINTAINER.permissions).add(ProjectRole.PROJECT_ADMIN.getPermissions()), "Owner", Color.PURPLE, 20),
    ORGANIZATION_OWNER("Organization_Owner", 25, Permission.IsOrganizationOwner.add(ProjectRole.PROJECT_OWNER.getPermissions()).add(ORGANIZATION_ADMIN.permissions), "Owner", Color.PURPLE, 10, false);

    private final String value;
    private final long roleId;
    private final Permission permissions;
    private final String title;
    private final Color color;
    private final boolean isAssignable;
    private final int rank;

    OrganizationRole(final String value, final long roleId, final Permission permissions, final String title, final Color color, final int rank) {
        this(value, roleId, permissions, title, color, rank, true);
    }

    OrganizationRole(final String value, final long roleId, final Permission permissions, final String title, final Color color, final int rank, final boolean isAssignable) {
        this.value = value;
        this.roleId = roleId;
        this.permissions = permissions;
        this.title = title;
        this.color = color;
        this.rank = rank;
        this.isAssignable = isAssignable;
        Role.registerRole(this);
    }

    @Override
    public @NotNull String getValue() {
        return this.value;
    }

    @Override
    public long getRoleId() {
        return this.roleId;
    }

    @Override
    public @NotNull RoleCategory getRoleCategory() {
        return RoleCategory.ORGANIZATION;
    }

    @Override
    public @NotNull Permission getPermissions() {
        return this.permissions;
    }

    @Override
    public @NotNull String getTitle() {
        return this.title;
    }

    @Override
    public @NotNull Color getColor() {
        return this.color;
    }

    @Override
    public boolean isAssignable() {
        return this.isAssignable;
    }

    @Override
    public Integer rank() {
        return this.rank;
    }

    @Override
    public @NotNull OrganizationRoleTable create(final Long organizationId, final UUID uuid, final long userId, final boolean isAccepted) {
        Preconditions.checkNotNull(organizationId, "organization id");
        return new OrganizationRoleTable(userId, this, isAccepted, organizationId, uuid);
    }
}
