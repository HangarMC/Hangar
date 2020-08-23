package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.Visibility;
import io.swagger.annotations.ApiModelProperty;
import io.papermc.hangar.model.Category;

/**
 * ModelsProtocolsAPIV2Project
 */
@Validated
public class Project {
    @JsonProperty("created_at")
    private OffsetDateTime createdAt = null;

    @JsonProperty("plugin_id")
    private String pluginId = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("namespace")
    private ProjectNamespace namespace = null;

    @JsonProperty("promoted_versions")
    @Valid
    private List<PromotedVersion> promotedVersions = null;

    @JsonProperty("stats")
    private ProjectStatsAll stats = null;

    @JsonProperty("category")
    private Category category = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("last_updated")
    private OffsetDateTime lastUpdated = null;

    @JsonProperty("visibility")
    private Visibility visibility = null;

    @JsonProperty("user_actions")
    private UserActions userActions = null;

    @JsonProperty("settings")
    private ProjectSettings settings = null;

    @JsonProperty("icon_url")
    private String iconUrl = "";

    // dummy values, only used for db -> model
    @JsonIgnore
    private String ownerName;
    @JsonIgnore
    private Long views;
    @JsonIgnore
    private Long downloads;
    @JsonIgnore
    private Long recentViews;
    @JsonIgnore
    private Long recentDownloads;
    @JsonIgnore
    private Long stars;
    @JsonIgnore
    private Long watchers;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectNamespace getNamespace() {
        if (namespace == null) {
            namespace = new ProjectNamespace().owner(ownerName).slug(name);
        }
        return namespace;
    }

    public void setNamespace(ProjectNamespace namespace) {
        this.namespace = namespace;
    }

    public List<PromotedVersion> getPromotedVersions() {
        return promotedVersions;
    }

    public void setPromotedVersions(List<PromotedVersion> promotedVersions) {
        this.promotedVersions = promotedVersions;
    }

    public ProjectStatsAll getStats() {
        if (stats == null) {
            stats = new ProjectStatsAll(views, downloads, recentViews, recentDownloads, stars, watchers);
        }
        return stats;
    }

    public void setStats(ProjectStatsAll stats) {
        this.stats = stats;
    }

    @EnumByOrdinal
    public Category getCategory() {
        return category;
    }

    @EnumByOrdinal
    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @EnumByOrdinal
    public Visibility getVisibility() {
        return visibility;
    }

    @EnumByOrdinal
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public UserActions getUserActions() {
        return userActions;
    }

    public void setUserActions(UserActions userActions) {
        this.userActions = userActions;
    }

    public ProjectSettings getSettings() {
        return settings;
    }

    public void setSettings(ProjectSettings settings) {
        this.settings = settings;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    // dummy values below

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public Long getDownloads() {
        return downloads;
    }

    public void setDownloads(Long downloads) {
        this.downloads = downloads;
    }

    public Long getRecentViews() {
        return recentViews;
    }

    public void setRecentViews(Long recentViews) {
        this.recentViews = recentViews;
    }

    public Long getRecentDownloads() {
        return recentDownloads;
    }

    public void setRecentDownloads(Long recentDownloads) {
        this.recentDownloads = recentDownloads;
    }

    public Long getStars() {
        return stars;
    }

    public void setStars(Long stars) {
        this.stars = stars;
    }

    public Long getWatchers() {
        return watchers;
    }

    public void setWatchers(Long watchers) {
        this.watchers = watchers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        if (!Objects.equals(createdAt, project.createdAt)) return false;
        if (!Objects.equals(pluginId, project.pluginId)) return false;
        if (!Objects.equals(name, project.name)) return false;
        if (!Objects.equals(namespace, project.namespace)) return false;
        if (!Objects.equals(promotedVersions, project.promotedVersions)) return false;
        if (!Objects.equals(stats, project.stats)) return false;
        if (category != project.category) return false;
        if (!Objects.equals(description, project.description)) return false;
        if (!Objects.equals(lastUpdated, project.lastUpdated)) return false;
        if (visibility != project.visibility) return false;
        if (!Objects.equals(userActions, project.userActions)) return false;
        if (!Objects.equals(settings, project.settings)) return false;
        return Objects.equals(iconUrl, project.iconUrl);
    }

    @Override
    public int hashCode() {
        int result = createdAt != null ? createdAt.hashCode() : 0;
        result = 31 * result + (pluginId != null ? pluginId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (promotedVersions != null ? promotedVersions.hashCode() : 0);
        result = 31 * result + (stats != null ? stats.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
        result = 31 * result + (userActions != null ? userActions.hashCode() : 0);
        result = 31 * result + (settings != null ? settings.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Project.class.getSimpleName() + "[", "]")
                .add("createdAt=" + createdAt)
                .add("pluginId='" + pluginId + "'")
                .add("name='" + name + "'")
                .add("namespace=" + namespace)
                .add("promotedVersions=" + promotedVersions)
                .add("stats=" + stats)
                .add("category=" + category)
                .add("description='" + description + "'")
                .add("lastUpdated=" + lastUpdated)
                .add("visibility=" + visibility)
                .add("userActions=" + userActions)
                .add("settings=" + settings)
                .add("iconUrl='" + iconUrl + "'")
                .toString();
    }
}
