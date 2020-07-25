package me.minidigger.hangar.model.viewhelpers;

import java.time.OffsetDateTime;

public class Author {

    private String name;
    private OffsetDateTime joinDate;
    private OffsetDateTime createdAt;
    private String role;
    private String donatorRole;
    private long projectCount;

    public Author() {
        //
    }

    public Author(String name, OffsetDateTime joinDate, OffsetDateTime createdAt, String role, String donatorRole, long projectCount) {
        this.name = name;
        this.joinDate = joinDate;
        this.createdAt = createdAt;
        this.role = role;
        this.donatorRole = donatorRole;
        this.projectCount = projectCount;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDonatorRole() {
        return donatorRole;
    }

    public void setDonatorRole(String donatorRole) {
        this.donatorRole = donatorRole;
    }

    public long getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(long projectCount) {
        this.projectCount = projectCount;
    }
}
