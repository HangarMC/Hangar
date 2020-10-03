package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

@Validated
public class Dependency {

    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "required", required = true)
    private boolean required;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("external_url")
    private String externalUrl;

    public Dependency(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public Dependency() { }

    /**
     * Get dependency name
     * @return name
     */
    @NotNull
    @ApiModelProperty(required = true, name = "Name as it appears in plugin.yml")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Is dependency required
     * @return is required
     */
    @ApiModelProperty(required = true, name = "Required dependency")
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Get Hangar projectId
     *
     * @return project id (if applicable)
     */
    @Nullable
    @ApiModelProperty("Hangar project id (if applicable)")
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * Get External URL
     * @return external url (if applicable)
     */
    @Nullable
    @ApiModelProperty("External URL (if applicable)")
    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    /**
     * Dependency has either a projectId or an externalUrl
     * @return true if projectId is not null or externalUrl is not null
     */
    @JsonIgnore
    public boolean isLinked() {
        return this.externalUrl != null || this.projectId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return required == that.required &&
                name.equals(that.name) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(externalUrl, that.externalUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, required, projectId, externalUrl);
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "name='" + name + '\'' +
                ", required=" + required +
                ", projectId=" + projectId +
                ", externalUrl='" + externalUrl + '\'' +
                '}';
    }
}
