package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class NewProject {

    private final long ownerId;
    @NotNull
    @Validate(SpEL = "#root.length le @hangarConfig.projects.maxNameLen", message = "Project name too long") // TODO i18n
    @Validate(SpEL = "#root matches @hangarConfig.projects.nameRegex", message = "Invalid project name") // TODO i18n
    private final String name;
    @NotNull
    @Validate(SpEL = "#root.length le @hangarConfig.projects.maxDescLen", message = "Project description too long") // TODO i18n
    private final String description;
    @NotNull
    private final Category category;
    private final String pageContent;
    private final String homepageUrl;
    private final String issuesUrl;
    private final String sourceUrl;
    private final String supportUrl;
    private final String licenseName;
    private final String licenseUrl;
    @NotNull
    @Validate(SpEL = "#root.length le @hangarConfig.projects.maxKeywords", message = "Too many keywords") // TODO i18n
    private final List<String> keywords;

    @JsonCreator
    public NewProject(long ownerId, @NotNull String name, @NotNull String description, @NotNull Category category, String pageContent, @JsonProperty("links") Map<String, String> linkMap, @JsonProperty("license") Map<String, String> licenseMap, @NotNull List<String> keywords) {
        this.ownerId = ownerId;
        this.name = StringUtils.compact(name);
        this.description = description;
        this.category = category;
        this.pageContent = pageContent;
        this.homepageUrl = linkMap.get("homepage");
        this.issuesUrl = linkMap.get("issues");
        this.sourceUrl = linkMap.get("source");
        this.supportUrl = linkMap.get("support");
        String licenseName = StringUtils.stringOrNull(licenseMap.get("customName"));
        if (licenseName == null) {
            licenseName = licenseMap.get("type");
        }
        this.licenseName = licenseName;
        this.licenseUrl = licenseMap.get("url");
        this.keywords = keywords;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public String getPageContent() {
        return pageContent;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public String getIssuesUrl() {
        return issuesUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    @Override
    public String toString() {
        return "NewProject{" +
                "ownerId=" + ownerId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", pageContent='" + pageContent + '\'' +
                ", homepageUrl='" + homepageUrl + '\'' +
                ", issuesUrl='" + issuesUrl + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", supportUrl='" + supportUrl + '\'' +
                ", licenseName='" + licenseName + '\'' +
                ", licenseUrl='" + licenseUrl + '\'' +
                ", keywords=" + keywords +
                '}';
    }
}
