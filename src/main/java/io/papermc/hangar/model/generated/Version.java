package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.Visibility;
import io.swagger.annotations.ApiModelProperty;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ModelsProtocolsAPIV2Version
 */
@Validated
public class Version {
    @JsonProperty("created_at")
    private OffsetDateTime createdAt = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("url_name")
    private String urlName = null;

    @JsonProperty("dependencies")
    @Valid
    private Map<Platform, List<Dependency>> dependencies = new EnumMap<>(Platform.class);

    @JsonProperty("visibility")
    private Visibility visibility = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("stats")
    @Nested("stat")
    private VersionStatsAll stats = null;

    @JsonProperty("file_info")
    @Nested("fi")
    private FileInfo fileInfo = null;

    @JsonProperty("author")
    private String author = null;

    @JsonProperty("review_state")
    private ReviewState reviewState = null;

    @JsonProperty("tags")
    @Valid
    @Nested("tag")
    private List<Tag> tags = new ArrayList<>();

    public Version createdAt(OffsetDateTime createdAt) {
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

    public Version name(String name) {
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

    public Version urlName(String urlName) {
        this.urlName = urlName;
        return this;
    }

    /**
     * Get url name
     * @return url name
     */
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getUrlName() {
        return urlName;
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }

    public Version dependencies(Map<Platform, List<Dependency>> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    /**
     * Get dependencies
     *
     * @return dependencies
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public Map<Platform, List<Dependency>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<Platform, List<Dependency>> dependencies) {
        this.dependencies = dependencies;
    }

    public Version visibility(Visibility visibility) {
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

    @EnumByOrdinal
    public Visibility getVisibility() {
        return visibility;
    }

    @EnumByOrdinal
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Version description(String description) {
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

    public Version stats(VersionStatsAll stats) {
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
    public VersionStatsAll getStats() {
        return stats;
    }

    public void setStats(VersionStatsAll stats) {
        this.stats = stats;
    }

    public Version fileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        return this;
    }

    /**
     * Get fileInfo
     *
     * @return fileInfo
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public Version author(String author) {
        this.author = author;
        return this;
    }

    /**
     * Get author
     *
     * @return author
     **/
    @ApiModelProperty(value = "")

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Version reviewState(ReviewState reviewState) {
        this.reviewState = reviewState;
        return this;
    }

    /**
     * Get reviewState
     *
     * @return reviewState
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @EnumByOrdinal
    @JsonFormat(shape = Shape.STRING)
    public ReviewState getReviewState() {
        return reviewState;
    }

    @EnumByOrdinal
    @JsonFormat(shape = Shape.STRING)
    public void setReviewState(ReviewState reviewState) {
        this.reviewState = reviewState;
    }

    public Version tags(List<Tag> tags) {
        this.tags = tags;
        return this;
    }

    public Version addTagsItem(Tag tagsItem) {
        this.tags.add(tagsItem);
        return this;
    }

    /**
     * Get tags
     *
     * @return tags
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    @Nested("tag")
    public List<Tag> getTags() {
        return tags;
    }

    @Nested("tag")
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        return Objects.equals(this.createdAt, version.createdAt) &&
               Objects.equals(this.name, version.name) &&
               Objects.equals(this.dependencies, version.dependencies) &&
               Objects.equals(this.visibility, version.visibility) &&
               Objects.equals(this.description, version.description) &&
               Objects.equals(this.stats, version.stats) &&
               Objects.equals(this.fileInfo, version.fileInfo) &&
               Objects.equals(this.author, version.author) &&
               Objects.equals(this.reviewState, version.reviewState) &&
               Objects.equals(this.tags, version.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, name, dependencies, visibility, description, stats, fileInfo, author, reviewState, tags);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2Version {\n");

        sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    dependencies: ").append(toIndentedString(dependencies)).append("\n");
        sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    stats: ").append(toIndentedString(stats)).append("\n");
        sb.append("    fileInfo: ").append(toIndentedString(fileInfo)).append("\n");
        sb.append("    author: ").append(toIndentedString(author)).append("\n");
        sb.append("    reviewState: ").append(toIndentedString(reviewState)).append("\n");
        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
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
