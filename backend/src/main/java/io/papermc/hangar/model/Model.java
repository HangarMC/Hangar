package io.papermc.hangar.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public abstract class Model {

    protected OffsetDateTime createdAt;

    protected Model(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final Model model = (Model) o;
        return Objects.equals(this.createdAt, model.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.createdAt);
    }

    @Override
    public String toString() {
        return "Model{" +
            "createdAt=" + this.createdAt +
            '}';
    }
}
