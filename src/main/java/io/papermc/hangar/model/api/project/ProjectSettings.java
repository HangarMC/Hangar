package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import org.jdbi.v3.core.mapper.Nested;
import org.jetbrains.annotations.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

public class ProjectSettings {

    @Validate(SpEL = "@validate.optionalRegex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String homepage;
    @Validate(SpEL = "@validate.optionalRegex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String issues;
    @Validate(SpEL = "@validate.optionalRegex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String source;
    @Validate(SpEL = "@validate.optionalRegex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String support;
    @Valid
    private final ProjectLicense license;
    @Valid
    private final ProjectDonationSettings donation;
    @NotNull
    @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxKeywords)", message = "project.new.error.tooManyKeywords")
    private final Collection<String> keywords;
    private final boolean forumSync;
    private final String sponsors;

    @JsonCreator
    public ProjectSettings(@Nullable String homepage, @Nullable String issues, @Nullable String source, @Nullable String support, @Nested("license") ProjectLicense license, @Nested("donation") ProjectDonationSettings donation, Collection<String> keywords, boolean forumSync, String sponsors) {
        this.homepage = homepage;
        this.issues = issues;
        this.source = source;
        this.support = support;
        this.license = license;
        this.donation = donation;
        this.keywords = keywords;
        this.forumSync = forumSync;
        this.sponsors = sponsors;
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

    public ProjectDonationSettings getDonation() {
        return donation;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public boolean isForumSync() {
        return forumSync;
    }

    public String getSponsors() {
        return sponsors;
    }

    @Override
    public String toString() {
        return "ProjectSettings{" +
                "homepage='" + homepage + '\'' +
                ", issues='" + issues + '\'' +
                ", source='" + source + '\'' +
                ", support='" + support + '\'' +
                ", license=" + license +
                ", donation=" + donation +
                ", keywords=" + keywords +
                ", forumSync=" + forumSync +
                ", sponsors='" + sponsors + '\'' +
                '}';
    }
}
