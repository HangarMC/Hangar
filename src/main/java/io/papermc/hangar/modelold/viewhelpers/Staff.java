package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.model.Role;

import java.time.OffsetDateTime;

public class Staff {

    private String name;
    private Role role;
    private OffsetDateTime joinDate;
    private OffsetDateTime createdAt;

    public Staff(String name, Role role, OffsetDateTime joinDate, OffsetDateTime createdAt) {
        this.name = name;
        this.role = role;
        this.joinDate = joinDate;
        this.createdAt = createdAt;
    }

    public Staff() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public OffsetDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(OffsetDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
