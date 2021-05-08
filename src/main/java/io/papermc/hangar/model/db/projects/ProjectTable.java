package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.ModelVisible;
import io.papermc.hangar.model.ProjectIdentified;
import io.papermc.hangar.model.Visitable;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectForm;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.Collection;

public class ProjectTable extends Table implements Visitable, ModelVisible, ProjectIdentified {

    private String name;
    private String slug;
    private String ownerName;
    private long ownerId;
    private Long topicId;
    private Long postId;
    private Category category;
    private String description;
    private Visibility visibility;
    private Collection<String> keywords;
    private String homepage;
    private String issues;
    private String source;
    private String support;
    private String licenseName;
    private String licenseUrl;
    private boolean forumSync;
    private boolean donationEnabled;
    private String donationEmail;
    private int donationDefaultAmount;
    private Collection<Integer> donationOnetimeAmounts;
    private Collection<Integer> donationMonthlyAmounts;

    public ProjectTable(ProjectOwner projectOwner, NewProjectForm form) {
        this.name = form.getName();
        this.slug = StringUtils.slugify(this.name);
        this.ownerName = projectOwner.getName();
        this.ownerId = projectOwner.getUserId();
        this.topicId = null;
        this.postId = null;
        this.category = form.getCategory();
        this.description = form.getDescription();
        this.visibility = Visibility.NEW;
        this.homepage = form.getSettings().getHomepage();
        this.issues = form.getSettings().getIssues();
        this.source = form.getSettings().getSource();
        this.support = form.getSettings().getSupport();
        this.keywords = form.getSettings().getKeywords();
        this.licenseName = form.getSettings().getLicense().getName();
        this.licenseUrl = form.getSettings().getLicense().getUrl();
        this.donationEnabled = form.getSettings().getDonation().isEnable();
        this.donationEmail = form.getSettings().getDonation().getEmail();
        this.donationDefaultAmount = form.getSettings().getDonation().getDefaultAmount();
        this.donationOnetimeAmounts = form.getSettings().getDonation().getOneTimeAmounts();
        this.donationMonthlyAmounts = form.getSettings().getDonation().getMonthlyAmounts();
    }

    protected ProjectTable(ProjectTable other) {
        this.name = other.name;
        this.slug = other.slug;
        this.ownerName = other.ownerName;
        this.ownerId = other.ownerId;
        this.topicId = other.topicId;
        this.postId = other.postId;
        this.category = other.category;
        this.description = other.description;
        this.visibility = other.visibility;
        this.keywords = other.keywords;
        this.homepage = other.homepage;
        this.issues = other.issues;
        this.source = other.source;
        this.support = other.support;
        this.licenseName = other.licenseName;
        this.licenseUrl = other.licenseUrl;
        this.forumSync = other.forumSync;
        this.donationEnabled = other.donationEnabled;
        this.donationEmail = other.donationEmail;
        this.donationDefaultAmount = other.donationDefaultAmount;
        this.donationOnetimeAmounts = other.donationOnetimeAmounts;
        this.donationMonthlyAmounts = other.donationMonthlyAmounts;
    }

    @JdbiConstructor
    public ProjectTable(OffsetDateTime createdAt, long id, String name, String slug, String ownerName, long ownerId, Long topicId,
                        Long postId, @EnumByOrdinal Category category, String description, @EnumByOrdinal Visibility visibility, Collection<String> keywords,
                        String homepage, String issues, String source, String support, String licenseName, String licenseUrl, boolean forumSync,
                        boolean donationEnabled, String donationEmail, int donationDefaultAmount, Collection<Integer> donationOnetimeAmounts, Collection<Integer> donationMonthlyAmounts) {
        super(createdAt, id);
        this.name = name;
        this.slug = slug;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.topicId = topicId;
        this.postId = postId;
        this.category = category;
        this.description = description;
        this.visibility = visibility;
        this.keywords = keywords;
        this.homepage = homepage;
        this.issues = issues;
        this.source = source;
        this.support = support;
        this.licenseName = licenseName;
        this.licenseUrl = licenseUrl;
        this.forumSync = forumSync;
        this.donationEnabled = donationEnabled;
        this.donationEmail = donationEmail;
        this.donationDefaultAmount = donationDefaultAmount;
        this.donationOnetimeAmounts = donationOnetimeAmounts;
        this.donationMonthlyAmounts = donationMonthlyAmounts;
    }

    // TODO remove a bunch of these setters and use a SettingsSave object or smth

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    @EnumByOrdinal
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    @EnumByOrdinal
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Collection<String> keywords) {
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

    public boolean isDonationEnabled() {
        return donationEnabled;
    }

    public void setDonationEnabled(boolean donationEnabled) {
        this.donationEnabled = donationEnabled;
    }

    public String getDonationEmail() {
        return donationEmail;
    }

    public void setDonationEmail(String donationEmail) {
        this.donationEmail = donationEmail;
    }

    public int getDonationDefaultAmount() {
        return donationDefaultAmount;
    }

    public void setDonationDefaultAmount(int donationDefaultAmount) {
        this.donationDefaultAmount = donationDefaultAmount;
    }

    public Collection<Integer> getDonationOnetimeAmounts() {
        return donationOnetimeAmounts;
    }

    public void setDonationOnetimeAmounts(Collection<Integer> donationOnetimeAmounts) {
        this.donationOnetimeAmounts = donationOnetimeAmounts;
    }

    public Collection<Integer> getDonationMonthlyAmounts() {
        return donationMonthlyAmounts;
    }

    public void setDonationMonthlyAmounts(Collection<Integer> donationMonthlyAmounts) {
        this.donationMonthlyAmounts = donationMonthlyAmounts;
    }

    @Override
    public long getProjectId() {
        return id;
    }

    @Override
    public String getUrl() {
        return "/" + this.getOwnerName() + "/" + this.getSlug();
    }

    @Override
    public String toString() {
        return "ProjectTable{" +
               "name='" + name + '\'' +
               ", slug='" + slug + '\'' +
               ", ownerName='" + ownerName + '\'' +
               ", ownerId=" + ownerId +
               ", topicId=" + topicId +
               ", postId=" + postId +
               ", category=" + category +
               ", description='" + description + '\'' +
               ", visibility=" + visibility +
               ", keywords=" + keywords +
               ", homepage='" + homepage + '\'' +
               ", issues='" + issues + '\'' +
               ", source='" + source + '\'' +
               ", support='" + support + '\'' +
               ", licenseName='" + licenseName + '\'' +
               ", licenseUrl='" + licenseUrl + '\'' +
               ", forumSync=" + forumSync +
               ", donationEnabled=" + donationEnabled +
               ", donationEmail='" + donationEmail + '\'' +
               ", donationDefaultAmount=" + donationDefaultAmount +
               ", donationOnetimeAmounts=" + donationOnetimeAmounts +
               ", donationMonthlyAmounts=" + donationMonthlyAmounts +
               "} " + super.toString();
    }
}
