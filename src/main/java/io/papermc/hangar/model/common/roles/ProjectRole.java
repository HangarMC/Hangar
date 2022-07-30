package io.papermc.hangar.model.common.roles;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import org.jetbrains.annotations.NotNull;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

import java.util.Arrays;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ProjectRole implements Role<ProjectRoleTable> {

    PROJECT_SUPPORT("Project_Support", 30, Permission.IsProjectMember, "Support", Color.TRANSPARENT, 60),
    PROJECT_EDITOR("Project_Editor", 31, Permission.EditPage.add(PROJECT_SUPPORT.getPermissions()), "Editor", Color.TRANSPARENT, 50),
    PROJECT_DEVELOPER("Project_Developer", 32, PROJECT_SUPPORT.getPermissions(), "Developer", Color.TRANSPARENT, 40),
    PROJECT_MAINTAINER("Project_Maintainer", 33, Permission.CreateVersion.add(Permission.EditVersion).add(Permission.DeleteVersion).add(Permission.EditTags).add(PROJECT_DEVELOPER.getPermissions()), "Maintainer", Color.TRANSPARENT, 30),
    PROJECT_ADMIN("Project_Admin", 34, Permission.IsProjectAdmin.add(Permission.EditApiKeys).add(PROJECT_MAINTAINER.getPermissions()), "Admin", Color.TRANSPARENT, 20),
    PROJECT_OWNER("Project_Owner", 35, Permission.IsProjectOwner.add(PROJECT_ADMIN.getPermissions()), "Owner", Color.TRANSPARENT, 10, false);

    private final String value;
    private final long roleId;
    private final Permission permissions;
    private final String title;
    private final Color color;
    private final boolean isAssignable;
    private final int rank;

    private static final ProjectRole[] VALUES = values();
    private static final List<ProjectRole> ASSIGNABLE_ROLES = Arrays.stream(VALUES).filter(ProjectRole::isAssignable).toList();

    public static ProjectRole[] getValues() {
        return VALUES;
    }

    public static List<ProjectRole> getAssignableRoles() {
        return ASSIGNABLE_ROLES;
    }

    ProjectRole(String value, long roleId, Permission permissions, String title, Color color, int rank) {
        this(value, roleId, permissions, title, color, rank, true);
    }

    ProjectRole(String value, long roleId, Permission permissions, String title, Color color, int rank, boolean isAssignable) {
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

    @Override
    public Integer getRank() {
        return rank;
    }

    @NotNull
    @Override
    public ProjectRoleTable create(Long projectId, long userId, boolean isAccepted) {
        Preconditions.checkNotNull(projectId, "project id");
        return new ProjectRoleTable(userId, this, isAccepted, projectId);
    }
}
