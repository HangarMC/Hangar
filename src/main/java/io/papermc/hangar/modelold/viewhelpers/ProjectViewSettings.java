package io.papermc.hangar.modelold.viewhelpers;

import java.util.Collection;
import java.util.List;

public class ProjectViewSettings {
    private Collection<String> keywords;
    private String homepage;
    private String issues;
    private String source;
    private String support;
    private String licenseName;
    private String licenseUrl;
    private boolean forumSync = true;

    public ProjectViewSettings(Collection<String> keywords, String homepage, String issues, String source, String support, String licenseName, String licenseUrl, boolean forumSync) {
        this.keywords = keywords;
        this.homepage = homepage;
        this.issues = issues;
        this.source = source;
        this.support = support;
        this.licenseName = licenseName;
        this.licenseUrl = licenseUrl;
        this.forumSync = forumSync;
    }

    public ProjectViewSettings() { }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getIssues() {
        return issues;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
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

    public boolean isForumSync() {
        return forumSync;
    }

    public void setForumSync(boolean forumSync) {
        this.forumSync = forumSync;
    }
}
