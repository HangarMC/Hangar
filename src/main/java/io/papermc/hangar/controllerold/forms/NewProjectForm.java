package io.papermc.hangar.controllerold.forms;

public class NewProjectForm {

    private String name;
    private String category;
    private String description;
    private long owner;

    private String homepageUrl;
    private String issueTrackerUrl;
    private String sourceUrl;
    private String externalSupportUrl;
    private String keywords;
    private String licenseType;
    private String licenseName;
    private String licenseUrl;

    private String pageContent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public void setHomepageUrl(String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }

    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getExternalSupportUrl() {
        return externalSupportUrl;
    }

    public void setExternalSupportUrl(String externalSupportUrl) {
        this.externalSupportUrl = externalSupportUrl;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getPageContent() {
        return pageContent;
    }

    public void setPageContent(String pageContent) {
        this.pageContent = pageContent;
    }

    @Override
    public String toString() {
        return "NewProjectForm{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", owner=" + owner +
                ", homepageUrl='" + homepageUrl + '\'' +
                ", issueTrackerUrl='" + issueTrackerUrl + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", externalSupportUrl='" + externalSupportUrl + '\'' +
                ", keywords='" + keywords + '\'' +
                ", licenseType='" + licenseType + '\'' +
                ", licenseName='" + licenseName + '\'' +
                ", licenseUrl='" + licenseUrl + '\'' +
                ", pageContent='" + pageContent + '\'' +
                '}';
    }
}
