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
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String homepage;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String issues;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String source;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String support;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String wiki;
    private final @Valid ProjectLicense license;
    private final @Valid ProjectDonationSettings donation;

    // @el(root: Collection<String>)
    private final @NotNull @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxKeywords)", message = "project.new.error.tooManyKeywords") Collection<String> keywords;
    private final boolean forumSync;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxSponsorsLen)", message = "project.new.error.tooLongSponsors") String sponsors;

    @JsonCreator
    public ProjectSettings(final @Nullable String homepage, final @Nullable String issues, final @Nullable String source, final @Nullable String support, final @Nullable String wiki, @Nested("license") final ProjectLicense license, @Nested("donation") final ProjectDonationSettings donation, final Collection<String> keywords, final boolean forumSync, final String sponsors) {
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
        return this.homepage;
    }

    public String getIssues() {
        return this.issues;
    }

    public String getSource() {
        return this.source;
    }

    public String getSupport() {
        return this.support;
    }

    public String getWiki() {
        return this.wiki;
    }

    public ProjectLicense getLicense() {
        return this.license;
    }

    public ProjectDonationSettings getDonation() {
        return this.donation;
    }

    public Collection<String> getKeywords() {
        return this.keywords;
    }

    public boolean isForumSync() {
        return this.forumSync;
    }

    public String getSponsors() {
        return this.sponsors;
    }

    @Override
    public String toString() {
        return "ProjectSettings{" +
                "homepage='" + this.homepage + '\'' +
                ", issues='" + this.issues + '\'' +
                ", source='" + this.source + '\'' +
                ", support='" + this.support + '\'' +
               ", support='" + this.wiki + '\'' +
                ", license=" + this.license +
                ", donation=" + this.donation +
                ", keywords=" + this.keywords +
                ", forumSync=" + this.forumSync +
                ", sponsors='" + this.sponsors + '\'' +
                '}';
    }
}
