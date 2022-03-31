package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DeployVersionInfo. Information about the version to create. Can be passed either as a file, or a string.
 */
@ApiModel(description = "DeployVersionInfo. Information about the version to create. Can be passed either as a file, or a string.")
@Validated
public class DeployVersionInfo {
    @JsonProperty("create_forum_post")
    private Boolean createForumPost = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("tags")
    @Valid
    private Map<String, Object> tags = null;

    public DeployVersionInfo createForumPost(Boolean createForumPost) {
        this.createForumPost = createForumPost;
        return this;
    }

    /**
     * If a post should be made on the forums after this version has been published.
     *
     * @return createForumPost
     **/
    @ApiModelProperty(value = "If a post should be made on the forums after this version has been published.")

    public Boolean isCreateForumPost() {
        return createForumPost;
    }

    public void setCreateForumPost(Boolean createForumPost) {
        this.createForumPost = createForumPost;
    }

    public DeployVersionInfo description(String description) {
        this.description = description;
        return this;
    }

    /**
     * The version description to post on both the version, and the forum post.
     *
     * @return description
     **/
    @ApiModelProperty(value = "The version description to post on both the version, and the forum post.")

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeployVersionInfo tags(Map<String, Object> tags) {
        this.tags = tags;
        return this;
    }

    public DeployVersionInfo putTagsItem(String key, Object tagsItem) {
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.put(key, tagsItem);
        return this;
    }

    /**
     * Override the default for the tags specified here. If nothing is specified for a tag, Hangar will try to infer
     * what it should be instead. Most tags only allow one value, but a few allow multiple. In cases where multiple
     * values are specified for a tag that only allows a single, the first one will be used.
     *
     * @return tags
     **/
    @ApiModelProperty(value = "Override the default for the tags specified here. If nothing is specified for a tag, Hangar will try to infer what it should be instead. Most tags only allow one value, but a few allow multiple. In cases where multiple values are specified for a tag that only allows a single, the first one will be used.")

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
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
        DeployVersionInfo deployVersionInfo = (DeployVersionInfo) o;
        return Objects.equals(this.createForumPost, deployVersionInfo.createForumPost) &&
               Objects.equals(this.description, deployVersionInfo.description) &&
               Objects.equals(this.tags, deployVersionInfo.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createForumPost, description, tags);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeployVersionInfo {\n");

        sb.append("    createForumPost: ").append(toIndentedString(createForumPost)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
