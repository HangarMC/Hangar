package io.papermc.hangar.db.modelold;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.Visitable;
import org.jdbi.v3.core.annotation.Unmappable;

import java.time.OffsetDateTime;

public class OrganizationsTable implements Visitable, ProjectOwner {

    private long id;
    private OffsetDateTime createdAt;
    private String name;
    private long ownerId;
    private long userId;

    public OrganizationsTable(long id, String name, long ownerId, long userId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.userId = userId;
    }

    public OrganizationsTable() { }

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

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Unmappable
    @Override
    @JsonIgnore
    public String getUrl() {
        return "/" + getName();
    }

    @Override
    public String toString() {
        return "OrganizationsTable{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", name='" + name + '\'' +
                ", ownerId=" + ownerId +
                ", userId=" + userId +
                '}';
    }
}
