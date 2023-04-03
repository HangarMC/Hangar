package io.papermc.hangar.model.internal.admin.health;

import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.projects.Visibility;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

public class UnhealthyProject implements Visible {

    private final ProjectNamespace namespace;
    private final OffsetDateTime lastUpdated;
    private final Visibility visibility;

    public UnhealthyProject(@Nested final ProjectNamespace namespace, final OffsetDateTime lastUpdated, @EnumByOrdinal final Visibility visibility) {
        this.namespace = namespace;
        this.lastUpdated = lastUpdated;
        this.visibility = visibility;
    }

    public ProjectNamespace getNamespace() {
        return this.namespace;
    }

    public OffsetDateTime getLastUpdated() {
        return this.lastUpdated;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public String toString() {
        return "UnhealthyProject{" +
            "namespace=" + this.namespace +
            ", lastUpdated=" + this.lastUpdated +
            ", visibility=" + this.visibility +
            "} " + super.toString();
    }
}
