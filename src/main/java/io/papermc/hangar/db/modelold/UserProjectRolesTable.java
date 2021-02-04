package io.papermc.hangar.db.modelold;


import io.papermc.hangar.modelold.Role;

import java.time.OffsetDateTime;

public class UserProjectRolesTable implements RoleTable {

    private long id;
    private OffsetDateTime createdAt;
    private long userId;
    private String roleType;
    private long projectId;
    private boolean isAccepted;
    private Role role;

    public UserProjectRolesTable(long userId, String roleType, long projectId, boolean isAccepted) {
        this.userId = userId;
        this.roleType = roleType;
        this.projectId = projectId;
        this.isAccepted = isAccepted;
    }

    public UserProjectRolesTable() { }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    @Override
    public Role getRole() {
        if (this.role == null) {
            role = Role.fromValue(roleType);
        }
        return role;
    }
}
