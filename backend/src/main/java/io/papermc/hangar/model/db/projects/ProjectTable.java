package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.ModelVisible;
import io.papermc.hangar.model.Owned;
import io.papermc.hangar.model.Visitable;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectForm;
import io.papermc.hangar.model.loggable.ProjectLoggable;
import io.papermc.hangar.util.StringUtils;
import java.time.OffsetDateTime;
import java.util.Collection;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectTable extends Table implements Visitable, ModelVisible, Owned, ProjectLoggable {

    private String name;
    private String slug;
    private String ownerName;
    private long ownerId;
    private Category category;
    private String description;
    private Visibility visibility;
    private Collection<String> tags;
    private Collection<String> keywords;
    private JSONB links;
    private String licenseType;
    private String licenseName;
    private String licenseUrl;
    private boolean donationEnabled;
    private String donationSubject;
    private String sponsors;

    public ProjectTable(final ProjectOwner projectOwner, final NewProjectForm form) {
        this.name = form.getName();
        this.slug = StringUtils.slugify(this.name);
        this.ownerName = projectOwner.getName();
        this.ownerId = projectOwner.getUserId();
        this.category = form.getCategory();
        this.description = form.getDescription();
        this.visibility = Visibility.NEW;
        this.links = new JSONB(form.getSettings().getLinks());
        this.tags = form.getSettings().getTags();
        this.keywords = form.getSettings().getKeywords();
        this.licenseType = form.getSettings().getLicense().getType();
        this.licenseName = form.getSettings().getLicense().getName();
        this.licenseUrl = form.getSettings().getLicense().getUrl();
        this.donationEnabled = form.getSettings().getDonation().isEnable();
        this.donationSubject = form.getSettings().getDonation().getSubject();
        this.sponsors = "";
    }

    protected ProjectTable(final ProjectTable other) {
        this.name = other.name;
        this.slug = other.slug;
        this.ownerName = other.ownerName;
        this.ownerId = other.ownerId;
        this.category = other.category;
        this.description = other.description;
        this.visibility = other.visibility;
        this.tags = other.tags;
        this.keywords = other.keywords;
        this.links = other.links;
        this.licenseType = other.licenseType;
        this.licenseName = other.licenseName;
        this.licenseUrl = other.licenseUrl;
        this.donationEnabled = other.donationEnabled;
        this.donationSubject = other.donationSubject;
        this.sponsors = other.sponsors;
    }

    @JdbiConstructor
    public ProjectTable(final OffsetDateTime createdAt, final long id, final String name, final String slug, final String ownerName, final long ownerId,
                        @EnumByOrdinal final Category category, final String description, @EnumByOrdinal final Visibility visibility, final Collection<String> tags,
                        final Collection<String> keywords, final JSONB links, final String licenseType, final String licenseName, final String licenseUrl,
                        final boolean donationEnabled, final String donationSubject, final String sponsors) {
        super(createdAt, id);
        this.name = name;
        this.slug = slug;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.category = category;
        this.description = description;
        this.visibility = visibility;
        this.tags = tags;
        this.keywords = keywords;
        this.links = links;
        this.licenseType = licenseType;
        this.licenseName = licenseName;
        this.licenseUrl = licenseUrl;
        this.donationEnabled = donationEnabled;
        this.donationSubject = donationSubject;
        this.sponsors = sponsors;
    }

    // TODO remove a bunch of these setters and use a SettingsSave object or smth

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSlug() {
        return this.slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(final long ownerId) {
        this.ownerId = ownerId;
    }

    @EnumByOrdinal
    public Category getCategory() {
        return this.category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    @EnumByOrdinal
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void setVisibility(final Visibility visibility) {
        this.visibility = visibility;
    }

    public Collection<String> getTags() {
        return this.tags;
    }

    public void setTags(final Collection<String> tags) {
        this.tags = tags;
    }

    public Collection<String> getKeywords() {
        return this.keywords;
    }

    public void setKeywords(final Collection<String> keywords) {
        this.keywords = keywords;
    }

    public JSONB getLinks() {
        return this.links;
    }

    public void setLinks(final JSONB links) {
        this.links = links;
    }

    public String getLicenseType() {
        return this.licenseType;
    }

    public void setLicenseType(final String licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseName() {
        return this.licenseName;
    }

    public void setLicenseName(final String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseUrl() {
        return this.licenseUrl;
    }

    public void setLicenseUrl(final String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public boolean isDonationEnabled() {
        return this.donationEnabled;
    }

    public void setDonationEnabled(final boolean donationEnabled) {
        this.donationEnabled = donationEnabled;
    }

    public String getDonationSubject() {
        return this.donationSubject;
    }

    public void setDonationSubject(final String donationSubject) {
        this.donationSubject = donationSubject;
    }

    public String getSponsors() {
        return this.sponsors;
    }

    public void setSponsors(final String sponsors) {
        this.sponsors = sponsors;
    }

    @Override
    public long getProjectId() {
        return this.id;
    }

    @Override
    public String getUrl() {
        return "/" + this.getOwnerName() + "/" + this.getSlug();
    }

    @Override
    public String toString() {
        return "ProjectTable{" +
            "name='" + this.name + '\'' +
            ", slug='" + this.slug + '\'' +
            ", ownerName='" + this.ownerName + '\'' +
            ", ownerId=" + this.ownerId +
            ", category=" + this.category +
            ", description='" + this.description + '\'' +
            ", visibility=" + this.visibility +
            ", tags=" + this.tags +
            ", keywords=" + this.keywords +
            ", links='" + this.links + '\'' +
            ", licenseType='" + this.licenseType + '\'' +
            ", licenseName='" + this.licenseName + '\'' +
            ", donationEnabled=" + this.donationEnabled +
            ", donationEmail='" + this.donationSubject + '\'' +
            ", sponsors='" + this.sponsors + '\'' +
            "} " + super.toString();
    }
}
