package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;

public class ProjectCompact extends Model implements Named, Visible {

    protected final String name;
    protected final ProjectNamespace namespace;
    protected final ProjectStats stats;
    protected final Category category;
    protected final OffsetDateTime lastUpdated;
    protected final Visibility visibility;

    public ProjectCompact(OffsetDateTime createdAt, String name, @Nested ProjectNamespace namespace, @Nested ProjectStats stats, @EnumByOrdinal Category category, OffsetDateTime lastUpdated, @EnumByOrdinal Visibility visibility) {
        super(createdAt);
        this.name = name;
        this.namespace = namespace;
        this.stats = stats;
        this.category = category;
        this.visibility = visibility;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String getName() {
        return name;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public ProjectStats getStats() {
        return stats;
    }

    public Category getCategory() {
        return category;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "ProjectCompact{" +
                "name='" + name + '\'' +
                ", namespace=" + namespace +
                ", stats=" + stats +
                ", category=" + category +
                ", visibility=" + visibility +
                "} " + super.toString();
    }
}
