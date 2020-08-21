package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2PromotedVersion
 */
@Validated
public class PromotedVersion {
    @JsonProperty("version")
    private String version = null;

    @JsonProperty("tags")
    @Valid
    private List<PromotedVersionTag> tags = new ArrayList<>();

    public PromotedVersion version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get version
     *
     * @return version
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public PromotedVersion tags(List<PromotedVersionTag> tags) {
        this.tags = tags;
        return this;
    }

    public PromotedVersion addTagsItem(PromotedVersionTag tagsItem) {
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
    public List<PromotedVersionTag> getTags() {
        return tags;
    }

    public void setTags(List<PromotedVersionTag> tags) {
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
        PromotedVersion promotedVersion = (PromotedVersion) o;
        return Objects.equals(this.version, promotedVersion.version) &&
               Objects.equals(this.tags, promotedVersion.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, tags);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2PromotedVersion {\n");

        sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
