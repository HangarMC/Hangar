package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.Visible;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.List;

public class Project extends Model implements Named, Visible {

    private final String name;
    private final ProjectNamespace namespace;
    private final List<PromotedVersion> promotedVersions;
    private final ProjectStats stats;
    private final Category category;
    private final String description;
    private final OffsetDateTime lastUpdated;
    private final Visibility visibility;
    private final UserActions userActions;
    private final ProjectSettings settings;
    private String iconUrl;

    @JdbiConstructor
    public Project(OffsetDateTime createdAt, String name, @Nested ProjectNamespace namespace, List<PromotedVersion> promotedVersions, @Nested ProjectStats stats, @EnumByOrdinal Category category, String description, OffsetDateTime lastUpdated, @EnumByOrdinal Visibility visibility, @Nested UserActions userActions, @Nested ProjectSettings settings) {
        super(createdAt);
        this.name = name;
        this.namespace = namespace;
        this.promotedVersions = promotedVersions;
        this.stats = stats;
        this.category = category;
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.visibility = visibility;
        this.userActions = userActions;
        this.settings = settings;
    }

    @Override
    public String getName() {
        return name;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public List<PromotedVersion> getPromotedVersions() {
        return promotedVersions;
    }

    public ProjectStats getStats() {
        return stats;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public Visibility getVisibility() {
        return visibility;
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
}
