package io.papermc.hangar.model.viewhelpers;

import java.time.OffsetDateTime;

import io.papermc.hangar.model.Role;

public class Author {

    private String name;
    private OffsetDateTime joinDate;
    private OffsetDateTime createdAt;
    private Role role;
    private Role donatorRole;
    private long count;

    public Author() {
        //
    }

    public Author(String name, OffsetDateTime joinDate, OffsetDateTime createdAt, Role role, Role donatorRole, long count) {
        this.name = name;
        this.joinDate = joinDate;
        this.createdAt = createdAt;
        this.role = role;
        this.donatorRole = donatorRole;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getDonatorRole() {
        return donatorRole;
    }

    public void setDonatorRole(Role donatorRole) {
        this.donatorRole = donatorRole;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getAvatarUrl() {
        return "https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png"; // TODO figure out what to do with avatar url
    }
}
