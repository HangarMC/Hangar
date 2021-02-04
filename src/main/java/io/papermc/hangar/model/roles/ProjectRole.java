package io.papermc.hangar.model.roles;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.Color;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public enum ProjectRole implements Role<ProjectRoleTable> {

    PROJECT_SUPPORT("Project_Support", 22, Permission.IsProjectMember, "Support", Color.TRANSPARENT),
    PROJECT_EDITOR("Project_Editor", 21, Permission.EditPage.add(PROJECT_SUPPORT.getPermissions()), "Editor", Color.TRANSPARENT),
    PROJECT_DEVELOPER("Project_Developer", 20, Permission.CreateVersion.add(Permission.EditVersion).add(Permission.EditTags).add(PROJECT_EDITOR.getPermissions()), "Developer", Color.TRANSPARENT),
    PROJECT_OWNER("Project_Owner", 19, Permission.IsProjectOwner.add(Permission.EditApiKeys).add(Permission.DeleteProject).add(Permission.DeleteVersion).add(PROJECT_DEVELOPER.getPermissions()), "Owner", Color.TRANSPARENT, false);

    private final String value;
    private final long roleId;
    private final Permission permissions;
    private final String title;
    private final Color color;
    private final boolean isAssignable;

    ProjectRole(String value, long roleId, Permission permissions, String title, Color color) {
        this(value, roleId, permissions, title, color, true);
    }

    ProjectRole(String value, long roleId, Permission permissions, String title, Color color, boolean isAssignable) {
        this.value = value;
        this.roleId = roleId;
        this.permissions = permissions;
        this.title = title;
        this.color = color;
        this.isAssignable = isAssignable;
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
        return RoleCategory.PROJECT;
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

    @Nullable
    @Override
    public Long getRank() {
        return null;
    }

    @NotNull
    @Override
    public ProjectRoleTable create(@Nullable Long principalId, long userId, boolean isAccepted) {
        Preconditions.checkNotNull(principalId, "project id");
        return new ProjectRoleTable(userId, this, isAccepted, principalId);
    }
}
