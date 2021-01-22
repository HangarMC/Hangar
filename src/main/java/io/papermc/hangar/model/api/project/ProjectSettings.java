package io.papermc.hangar.model.api.project;

import org.jdbi.v3.core.mapper.Nested;
import org.jetbrains.annotations.Nullable;

public class ProjectSettings {

    private final String homepage;
    private final String issues;
    private final String source;
    private final String support;
    private final ProjectLicense license;
    private final boolean forumSync;

    public ProjectSettings(@Nullable String homepage, @Nullable String issues, @Nullable String source, @Nullable String support, @Nullable @Nested("license") ProjectLicense license, boolean forumSync) {
        this.homepage = homepage;
        this.issues = issues;
        this.source = source;
        this.support = support;
        this.license = license;
        this.forumSync = forumSync;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getIssues() {
        return issues;
    }

    public String getSource() {
        return source;
    }

    public String getSupport() {
        return support;
    }

    public ProjectLicense getLicense() {
        return license;
    }

    public boolean isForumSync() {
        return forumSync;
    }
}
