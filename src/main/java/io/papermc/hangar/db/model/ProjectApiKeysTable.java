package io.papermc.hangar.db.model;


import java.time.OffsetDateTime;

public class ProjectApiKeysTable {

    private long id;
    private OffsetDateTime createdAt;
    private long projectId;
    private String value;

    public ProjectApiKeysTable(long projectId, String value) {
        this.projectId = projectId;
        this.value = value;
    }

    public ProjectApiKeysTable() { }

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


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
