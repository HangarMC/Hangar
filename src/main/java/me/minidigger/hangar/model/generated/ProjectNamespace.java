package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2ProjectNamespace
 */
@Validated
public class ProjectNamespace {
    @JsonProperty("owner")
    private String owner = null;

    @JsonProperty("slug")
    private String slug = null;

    public ProjectNamespace owner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Get owner
     *
     * @return owner
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ProjectNamespace slug(String slug) {
        this.slug = slug;
        return this;
    }

    /**
     * Get slug
     *
     * @return slug
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectNamespace projectNamespace = (ProjectNamespace) o;
        return Objects.equals(this.owner, projectNamespace.owner) &&
               Objects.equals(this.slug, projectNamespace.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, slug);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2ProjectNamespace {\n");

        sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
        sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
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
