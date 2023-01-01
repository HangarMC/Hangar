package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

public class ProjectCompact extends Model implements Named, Visible {

    protected final long id;
    protected final String name;
    protected final ProjectNamespace namespace;
    protected final ProjectStats stats;
    protected final Category category;
    protected final OffsetDateTime lastUpdated;
    protected final Visibility visibility;
    protected String avatarUrl;

    public ProjectCompact(final OffsetDateTime createdAt, final long id, final String name, @Nested final ProjectNamespace namespace, @Nested final ProjectStats stats, @EnumByOrdinal final Category category, final OffsetDateTime lastUpdated, @EnumByOrdinal final Visibility visibility) {
        super(createdAt);
        this.id = id;
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

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @JsonIgnore
    public long getId() {
        return this.id;
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
