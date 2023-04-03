package io.papermc.hangar.model.api.project.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.api.project.ProjectDonationSettings;
import io.papermc.hangar.model.api.project.ProjectLicense;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectSettings {

    // @el(root: List<String>)
    private final @NotNull @Validate(SpEL = "@validate.max(#root, 4)", message = "Too many link sections") List<@Valid LinkSection> links;
    // @el(root: Collection<String>)
    private final @NotNull @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxTags)", message = "project.new.error.tooManyTags") Collection<String> tags;
    private final @Valid ProjectLicense license;

    // @el(root: Collection<String>)
    private final @NotNull @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxKeywords)", message = "project.new.error.tooManyKeywords") Collection<String> keywords;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxSponsorsLen)", message = "project.new.error.tooLongSponsors") String sponsors;

    @JdbiConstructor
    public ProjectSettings(final JSONB links, final List<String> tags, @Nested("license") final ProjectLicense license, final Collection<String> keywords, final String sponsors) {
        this.links = links.get(new TypeReference<>() {
        });
        this.tags = tags;
        this.license = license;
        this.keywords = keywords;
        this.sponsors = sponsors;
    }

    @JsonCreator
    public ProjectSettings(@Nested("links") final List<LinkSection> links, final Collection<String> tags, @Nested("license") final ProjectLicense license, final Collection<String> keywords, final String sponsors) {
        this.links = links;
        this.tags = tags;
        this.license = license;
        this.keywords = keywords;
        this.sponsors = sponsors;
    }

    public List<LinkSection> getLinks() {
        return this.links;
    }

    public ProjectLicense getLicense() {
        return this.license;
    }

    public Collection<String> getTags() {
        return this.tags;
    }

    @Deprecated
    public ProjectDonationSettings getDonation() {
        return new ProjectDonationSettings(false, "");
    }

    public Collection<String> getKeywords() {
        return this.keywords;
    }

    public String getSponsors() {
        return this.sponsors;
    }

    @Override
    public String toString() {
        return "ProjectSettings{" +
            "links=" + this.links +
            ", tags=" + this.tags +
            ", license=" + this.license +
            ", keywords=" + this.keywords +
            ", sponsors='" + this.sponsors + '\'' +
            '}';
    }
}
