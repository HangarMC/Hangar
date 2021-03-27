package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.api.project.version.PromotedVersion;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.List;

public class Project extends ProjectCompact {

    private final String description;
    private final OffsetDateTime lastUpdated;
    private final UserActions userActions;
    private final ProjectSettings settings;
    // TODO remove this as they should all be standardized based on the project namespace
    private final String iconUrl;
    protected final List<PromotedVersion> promotedVersions;

    @JdbiConstructor
    public Project(OffsetDateTime createdAt, String name, @Nested ProjectNamespace namespace, @Nested ProjectStats stats, @EnumByOrdinal Category category, String description, OffsetDateTime lastUpdated, @EnumByOrdinal Visibility visibility, @Nested UserActions userActions, @Nested ProjectSettings settings, List<PromotedVersion> promotedVersions) {
        super(createdAt, name, namespace, stats, category, visibility);
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.userActions = userActions;
        this.settings = settings;
        this.promotedVersions = promotedVersions;
        this.iconUrl = "/api/v1/project/" + this.getNamespace().getOwner() + "/" + this.getNamespace().getSlug() + "/icon";
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public UserActions getUserActions() {
        return userActions;
    }

    public ProjectSettings getSettings() {
        return settings;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public List<PromotedVersion> getPromotedVersions() {
        return promotedVersions;
    }

    @Override
    public String toString() {
        return "Project{" +
                "description='" + description + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", userActions=" + userActions +
                ", settings=" + settings +
                ", iconUrl='" + iconUrl + '\'' +
                ", promotedVersions=" + promotedVersions +
                "} " + super.toString();
    }
}
