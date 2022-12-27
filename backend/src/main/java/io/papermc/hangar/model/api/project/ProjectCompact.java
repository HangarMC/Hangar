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

    public ProjectCompact(final OffsetDateTime createdAt, final String name, @Nested final ProjectNamespace namespace, @Nested final ProjectStats stats, @EnumByOrdinal final Category category, final OffsetDateTime lastUpdated, @EnumByOrdinal final Visibility visibility) {
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
        return this.name;
    }

    public ProjectNamespace getNamespace() {
        return this.namespace;
    }

    public ProjectStats getStats() {
        return this.stats;
    }

    public Category getCategory() {
        return this.category;
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
        return "ProjectCompact{" +
                "name='" + this.name + '\'' +
                ", namespace=" + this.namespace +
                ", stats=" + this.stats +
                ", category=" + this.category +
                ", visibility=" + this.visibility +
                "} " + super.toString();
    }
}
