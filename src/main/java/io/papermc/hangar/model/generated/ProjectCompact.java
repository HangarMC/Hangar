package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import io.papermc.hangar.model.Category;

/**
 * ModelsProtocolsAPIV2CompactProject
 */
@Validated
public class ProjectCompact {
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

    /**
     * Gets or Sets visibility
     */
    public enum VisibilityEnum {
        PUBLIC("public"),

        NEW("new"),

        NEEDSCHANGES("needsChanges"),

        NEEDSAPPROVAL("needsApproval"),

        SOFTDELETE("softDelete");

        private final String value;

        VisibilityEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static VisibilityEnum fromValue(String text) {
            for (VisibilityEnum b : VisibilityEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("visibility")
    private VisibilityEnum visibility = null;

    public ProjectCompact pluginId(String pluginId) {
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

    public ProjectCompact name(String name) {
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

    public ProjectCompact namespace(ProjectNamespace namespace) {
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

    @Nested
    public void setNamespace(ProjectNamespace namespace) {
        this.namespace = namespace;
    }

    public ProjectCompact promotedVersions(List<PromotedVersion> promotedVersions) {
        this.promotedVersions = promotedVersions;
        return this;
    }

    public ProjectCompact addPromotedVersionsItem(PromotedVersion promotedVersionsItem) {
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

    public ProjectCompact stats(ProjectStatsAll stats) {
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

    @Nested
    public void setStats(ProjectStatsAll stats) {
        this.stats = stats;
    }

    public ProjectCompact category(Category category) {
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

    @EnumByOrdinal
    public void setCategory(Category category) {
        this.category = category;
    }

    public ProjectCompact visibility(VisibilityEnum visibility) {
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

    public VisibilityEnum getVisibility() {
        return visibility;
    }

    @EnumByOrdinal
    public void setVisibility(VisibilityEnum visibility) {
        this.visibility = visibility;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectCompact projectCompact = (ProjectCompact) o;
        return Objects.equals(this.pluginId, projectCompact.pluginId) &&
               Objects.equals(this.name, projectCompact.name) &&
               Objects.equals(this.namespace, projectCompact.namespace) &&
               Objects.equals(this.promotedVersions, projectCompact.promotedVersions) &&
               Objects.equals(this.stats, projectCompact.stats) &&
               Objects.equals(this.category, projectCompact.category) &&
               Objects.equals(this.visibility, projectCompact.visibility);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, name, namespace, promotedVersions, stats, category, visibility);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2CompactProject {\n");

        sb.append("    pluginId: ").append(toIndentedString(pluginId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
        sb.append("    promotedVersions: ").append(toIndentedString(promotedVersions)).append("\n");
        sb.append("    stats: ").append(toIndentedString(stats)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
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
