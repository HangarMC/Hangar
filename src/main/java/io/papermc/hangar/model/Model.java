package io.papermc.hangar.model;

import java.time.OffsetDateTime;

public abstract class Model {

    protected final OffsetDateTime createdAt;

    public Model(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Model{" +
                "createdAt=" + createdAt +
                '}';
    }
}
