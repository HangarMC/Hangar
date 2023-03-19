package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.core.mapper.Nested;

public class ProjectSettings {

    // TODO fixme
    private final @Valid List<LinkSection> links = List.of(
            new LinkSection(0, "top", "Top", List.of(
                    new Link(0, "Wiki", "https://github.com"),
                    new Link(0, "Homepage", "https://google.com")
            )),
            new LinkSection(0, "sidebar", "Donations", List.of(
                    new Link(0, "Paypal", "https://paypal.com"),
                    new Link(0, "OpenCollective", "https://opencollective.com")
            ))
    );

    record LinkSection(long id, String type, String title, List<Link> links) {
    }

    record Link(long id, String name, String url) {
    }

    private final @Valid ProjectLicense license;
    private final @Valid ProjectDonationSettings donation;

    // @el(root: Collection<String>)
    private final @NotNull @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxKeywords)", message = "project.new.error.tooManyKeywords") Collection<String> keywords;
    private final boolean forumSync;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxSponsorsLen)", message = "project.new.error.tooLongSponsors") String sponsors;

    @JsonCreator
    public ProjectSettings(@Nested("license") final ProjectLicense license, @Nested("donation") final ProjectDonationSettings donation, final Collection<String> keywords, final boolean forumSync, final String sponsors) {
        this.license = license;
        this.donation = donation;
        this.keywords = keywords;
        this.forumSync = forumSync;
        this.sponsors = sponsors;
    }

    public List<LinkSection> getLinks() {
        return this.links;
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
            "links=" + this.links +
            ", license=" + this.license +
            ", donation=" + this.donation +
            ", keywords=" + this.keywords +
            ", forumSync=" + this.forumSync +
            ", sponsors='" + this.sponsors + '\'' +
            '}';
    }
}
