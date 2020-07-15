package me.minidigger.hangar.model.db;


import java.time.LocalDateTime;

public class ProjectChannelsTable {

    private long id;
    private LocalDateTime createdAt;
    private String name;
    private long color;
    private long projectId;
    private boolean isNonReviewed;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
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
