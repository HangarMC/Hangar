package io.papermc.hangar.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public abstract class Model {

    protected OffsetDateTime createdAt;

    protected Model(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return createdAt.equals(model.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt);
    }

    @Override
    public String toString() {
        return "Model{" +
                "createdAt=" + createdAt +
                '}';
    }
}
