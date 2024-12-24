package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.util.AvatarUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

public class ProjectCompact extends Model implements Named, Visible, Identified {

    @Schema(description = "The internal id of the project")
    protected final long id;
    @Schema(description = "The unique name of the project")
    protected final String name;
    @Schema(description = "The namespace of the project")
    protected final ProjectNamespace namespace;
    @Schema(description = "Stats of the project")
    protected final ProjectStats stats;
    @Schema(description = "The category of the project")
    protected final Category category;
    @Schema(description = "The last time the project was updated")
    protected final OffsetDateTime lastUpdated;
    @Schema(description = "The visibility of the project")
    protected final Visibility visibility;
    @Schema(description = "The url to the project's icon")
    protected String avatarUrl;
    @Schema(description = "The short description of the project")
    protected final String description;

    public ProjectCompact(final OffsetDateTime createdAt, final long id, final String name, @Nested final ProjectNamespace namespace, final String description, @Nested final ProjectStats stats, @EnumByOrdinal final Category category, final OffsetDateTime lastUpdated, @EnumByOrdinal final Visibility visibility, @Nullable final String avatar, @Nullable final String avatarFallback) {
        super(createdAt);
        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.description = description;
        this.stats = stats;
        this.category = category;
        this.visibility = visibility;
        this.lastUpdated = lastUpdated;
        this.setAvatarUrl(AvatarUtil.avatarUrl(avatar, avatarFallback));
    }

    @Override
    public long getId() {
        return this.id;
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

    public String getDescription() {
        return this.description;
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

    @Override
    public String toString() {
        return "ProjectCompact{" +
            "id=" + this.id +
            ", name='" + this.name + '\'' +
            ", namespace=" + this.namespace +
            ", description=" + this.description +
            ", stats=" + this.stats +
            ", category=" + this.category +
            ", visibility=" + this.visibility +
            "} " + super.toString();
    }
}
