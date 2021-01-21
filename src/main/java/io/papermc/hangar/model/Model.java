package io.papermc.hangar.model;

import java.time.OffsetDateTime;

public abstract class Model {

    protected OffsetDateTime createdAt;

    public Model(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
