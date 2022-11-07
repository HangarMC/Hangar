package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.jdbi.v3.core.mapper.Nested;
import org.jetbrains.annotations.Nullable;

public class ProjectSettings {

    // @el(root: String)
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String homepage;

    // @el(root: String)
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String issues;

    // @el(root: String)
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String source;

    // @el(root: String)
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String support;

    // @el(root: String)
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String wiki;
    @Valid
    private final ProjectLicense license;
    @Valid
    private final ProjectDonationSettings donation;

    // @el(root: Collection<String>)
    @NotNull
    @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxKeywords)", message = "project.new.error.tooManyKeywords")
    private final Collection<String> keywords;
    private final boolean forumSync;

    // @el(root: String)
    @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxSponsorsLen)", message = "project.new.error.tooLongSponsors")
    private final String sponsors;

    @JsonCreator
    public ProjectSettings(@Nullable String homepage, @Nullable String issues, @Nullable String source, @Nullable String support, @Nullable String wiki, @Nested("license") ProjectLicense license, @Nested("donation") ProjectDonationSettings donation, Collection<String> keywords, boolean forumSync, String sponsors) {
        this.homepage = homepage;
        this.issues = issues;
        this.source = source;
        this.support = support;
        this.wiki = wiki;
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

    public String getWiki() {
        return wiki;
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
               ", support='" + wiki + '\'' +
                ", license=" + license +
                ", donation=" + donation +
                ", keywords=" + keywords +
                ", forumSync=" + forumSync +
                ", sponsors='" + sponsors + '\'' +
                '}';
    }
}
