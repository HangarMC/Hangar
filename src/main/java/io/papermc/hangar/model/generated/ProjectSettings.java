package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2ProjectSettings
 */
@Validated
public class ProjectSettings {
    @JsonProperty("homepage")
    private String homepage = null;

    @JsonProperty("issues")
    private String issues = null;

    @JsonProperty("sources")
    private String sources = null;

    @JsonProperty("support")
    private String support = null;

    @JsonProperty("license")
    private ProjectLicense license = null;

    @JsonProperty("forum_sync")
    private Boolean forumSync = null;

    public ProjectSettings homepage(String homepage) {
        this.homepage = homepage;
        return this;
    }

    /**
     * Get homepage
     *
     * @return homepage
     **/
    @ApiModelProperty(value = "")

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public ProjectSettings issues(String issues) {
        this.issues = issues;
        return this;
    }

    /**
     * Get issues
     *
     * @return issues
     **/
    @ApiModelProperty(value = "")

    public String getIssues() {
        return issues;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }

    public ProjectSettings sources(String sources) {
        this.sources = sources;
        return this;
    }

    /**
     * Get sources
     *
     * @return sources
     **/
    @ApiModelProperty(value = "")

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public ProjectSettings support(String support) {
        this.support = support;
        return this;
    }

    /**
     * Get support
     *
     * @return support
     **/
    @ApiModelProperty(value = "")

    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public ProjectSettings license(ProjectLicense license) {
        this.license = license;
        return this;
    }

    /**
     * Get license
     *
     * @return license
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public ProjectLicense getLicense() {
        return license;
    }

    public void setLicense(ProjectLicense license) {
        this.license = license;
    }

    public ProjectSettings forumSync(Boolean forumSync) {
        this.forumSync = forumSync;
        return this;
    }

    /**
     * Get forumSync
     *
     * @return forumSync
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Boolean isForumSync() {
        return forumSync;
    }

    public void setForumSync(Boolean forumSync) {
        this.forumSync = forumSync;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectSettings projectSettings = (ProjectSettings) o;
        return Objects.equals(this.homepage, projectSettings.homepage) &&
               Objects.equals(this.issues, projectSettings.issues) &&
               Objects.equals(this.sources, projectSettings.sources) &&
               Objects.equals(this.support, projectSettings.support) &&
               Objects.equals(this.license, projectSettings.license) &&
               Objects.equals(this.forumSync, projectSettings.forumSync);
    }

    @Override
    public int hashCode() {
        return Objects.hash(homepage, issues, sources, support, license, forumSync);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2ProjectSettings {\n");

        sb.append("    homepage: ").append(toIndentedString(homepage)).append("\n");
        sb.append("    issues: ").append(toIndentedString(issues)).append("\n");
        sb.append("    sources: ").append(toIndentedString(sources)).append("\n");
        sb.append("    support: ").append(toIndentedString(support)).append("\n");
        sb.append("    license: ").append(toIndentedString(license)).append("\n");
        sb.append("    forumSync: ").append(toIndentedString(forumSync)).append("\n");
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
