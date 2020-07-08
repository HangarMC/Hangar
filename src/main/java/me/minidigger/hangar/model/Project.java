package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;
import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

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
    private List<PromotedVersion> promotedVersions = new ArrayList<>();

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
    private String iconUrl = null;

    public Project createdAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Get createdAt
     *
     * @return createdAt
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Project pluginId(String pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    /**
     * Get pluginId
     *
     * @return pluginId
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public Project name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Project namespace(ProjectNamespace namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Get namespace
     *
     * @return namespace
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public void setNamespace(ProjectNamespace namespace) {
        this.namespace = namespace;
    }

    public Project promotedVersions(List<PromotedVersion> promotedVersions) {
        this.promotedVersions = promotedVersions;
        return this;
    }

    public Project addPromotedVersionsItem(PromotedVersion promotedVersionsItem) {
        this.promotedVersions.add(promotedVersionsItem);
        return this;
    }

    /**
     * Get promotedVersions
     *
     * @return promotedVersions
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public List<PromotedVersion> getPromotedVersions() {
        return promotedVersions;
    }

    public void setPromotedVersions(List<PromotedVersion> promotedVersions) {
        this.promotedVersions = promotedVersions;
    }

    public Project stats(ProjectStatsAll stats) {
        this.stats = stats;
        return this;
    }

    /**
     * Get stats
     *
     * @return stats
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public ProjectStatsAll getStats() {
        return stats;
    }

    public void setStats(ProjectStatsAll stats) {
        this.stats = stats;
    }

    public Project category(Category category) {
        this.category = category;
        return this;
    }

    /**
     * Get category
     *
     * @return category
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Project description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get description
     *
     * @return description
     **/
    @ApiModelProperty(value = "")

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Project lastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    /**
     * Get lastUpdated
     *
     * @return lastUpdated
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Project visibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    /**
     * Get visibility
     *
     * @return visibility
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Project userActions(UserActions userActions) {
        this.userActions = userActions;
        return this;
    }

    /**
     * Get userActions
     *
     * @return userActions
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public UserActions getUserActions() {
        return userActions;
    }

    public void setUserActions(UserActions userActions) {
        this.userActions = userActions;
    }

    public Project settings(ProjectSettings settings) {
        this.settings = settings;
        return this;
    }

    /**
     * Get settings
     *
     * @return settings
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public ProjectSettings getSettings() {
        return settings;
    }

    public void setSettings(ProjectSettings settings) {
        this.settings = settings;
    }

    public Project iconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    /**
     * Get iconUrl
     *
     * @return iconUrl
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Project project = (Project) o;
        return Objects.equals(this.createdAt, project.createdAt) &&
               Objects.equals(this.pluginId, project.pluginId) &&
               Objects.equals(this.name, project.name) &&
               Objects.equals(this.namespace, project.namespace) &&
               Objects.equals(this.promotedVersions, project.promotedVersions) &&
               Objects.equals(this.stats, project.stats) &&
               Objects.equals(this.category, project.category) &&
               Objects.equals(this.description, project.description) &&
               Objects.equals(this.lastUpdated, project.lastUpdated) &&
               Objects.equals(this.visibility, project.visibility) &&
               Objects.equals(this.userActions, project.userActions) &&
               Objects.equals(this.settings, project.settings) &&
               Objects.equals(this.iconUrl, project.iconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, pluginId, name, namespace, promotedVersions, stats, category, description, lastUpdated, visibility, userActions, settings, iconUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2Project {\n");

        sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
        sb.append("    pluginId: ").append(toIndentedString(pluginId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
        sb.append("    promotedVersions: ").append(toIndentedString(promotedVersions)).append("\n");
        sb.append("    stats: ").append(toIndentedString(stats)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    lastUpdated: ").append(toIndentedString(lastUpdated)).append("\n");
        sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
        sb.append("    userActions: ").append(toIndentedString(userActions)).append("\n");
        sb.append("    settings: ").append(toIndentedString(settings)).append("\n");
        sb.append("    iconUrl: ").append(toIndentedString(iconUrl)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
