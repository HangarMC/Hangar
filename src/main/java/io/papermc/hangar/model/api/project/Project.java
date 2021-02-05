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
    private String iconUrl;

    @JdbiConstructor
    public Project(OffsetDateTime createdAt, String name, @Nested ProjectNamespace namespace, List<PromotedVersion> promotedVersions, @Nested ProjectStats stats, @EnumByOrdinal Category category, String description, OffsetDateTime lastUpdated, @EnumByOrdinal Visibility visibility, @Nested UserActions userActions, @Nested ProjectSettings settings) {
        super(createdAt, name, namespace, promotedVersions, stats, category, visibility);
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.userActions = userActions;
        this.settings = settings;
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

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public String toString() {
        return "Project{" +
                "description='" + description + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", userActions=" + userActions +
                ", settings=" + settings +
                ", iconUrl='" + iconUrl + '\'' +
                "} " + super.toString();
    }
}
