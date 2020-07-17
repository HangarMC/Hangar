package me.minidigger.hangar.db.model;


import java.time.OffsetDateTime;

public class ProjectChannelsTable {

    private long id;
    private OffsetDateTime createdAt;
    private String name;
    private long color;
    private long projectId;
    private boolean isNonReviewed;

    public ProjectChannelsTable() {
        //
    }

    public ProjectChannelsTable(String name, long color, long projectId) {
        this.name = name;
        this.color = color;
        this.projectId = projectId;
        this.isNonReviewed = false;
    }

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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getColor() {
        return color;
    }

    public void setColor(long color) {
        this.color = color;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public boolean getIsNonReviewed() {
        return isNonReviewed;
    }

    public void setIsNonReviewed(boolean isNonReviewed) {
        this.isNonReviewed = isNonReviewed;
    }

}
