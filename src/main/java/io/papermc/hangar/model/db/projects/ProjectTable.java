package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.Visitable;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.internal.api.requests.projects.NewProject;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.Collection;

public class ProjectTable extends Table implements Visitable, Visible {

    private String name;
    private String slug;
    private String ownerName;
    private Long recommendedVersionId;
    private long ownerId;
    private long topicId;
    private long postId;
    private Category category;
    private String description;
    private Visibility visibility;
    private JSONB notes;
    private Collection<String> keywords;
    private String homepage;
    private String issues;
    private String source;
    private String support;
    private String licenseName;
    private String licenseUrl;
    private boolean forumSync;

    public ProjectTable(ProjectOwner projectOwner, NewProject newProject) {
        this.name = newProject.getName();
        this.slug = StringUtils.slugify(this.name);
        this.ownerName = projectOwner.getName();
        this.ownerId = projectOwner.getUserId();
        this.category = newProject.getCategory();
        this.description = newProject.getDescription();
        this.visibility = Visibility.NEW;
        this.homepage = newProject.getHomepageUrl();
        this.issues = newProject.getIssuesUrl();
        this.source = newProject.getSourceUrl();
        this.support = newProject.getSupportUrl();
        this.keywords = newProject.getKeywords();
        this.licenseName = newProject.getLicenseName();
        this.licenseUrl = newProject.getLicenseUrl();
    }

    protected ProjectTable(ProjectTable other) {
        this.name = other.name;
        this.slug = other.slug;
        this.ownerName = other.ownerName;
        this.recommendedVersionId = other.recommendedVersionId;
        this.ownerId = other.ownerId;
        this.topicId = other.topicId;
        this.postId = other.postId;
        this.category = other.category;
        this.description = other.description;
        this.visibility = other.visibility;
        this.notes = other.notes;
        this.keywords = other.keywords;
        this.homepage = other.homepage;
        this.issues = other.issues;
        this.source = other.source;
        this.support = other.support;
        this.licenseName = other.licenseName;
        this.licenseUrl = other.licenseUrl;
        this.forumSync = other.forumSync;
    }

    @JdbiConstructor
    public ProjectTable(OffsetDateTime createdAt, long id, String name, String slug, String ownerName, Long recommendedVersionId, long ownerId, long topicId, long postId, @EnumByOrdinal Category category, String description, @EnumByOrdinal Visibility visibility, JSONB notes, Collection<String> keywords, String homepage, String issues, String source, String support, String licenseName, String licenseUrl, boolean forumSync) {
        super(createdAt, id);
        this.name = name;
        this.slug = slug;
        this.ownerName = ownerName;
        this.recommendedVersionId = recommendedVersionId;
        this.ownerId = ownerId;
        this.topicId = topicId;
        this.postId = postId;
        this.category = category;
        this.description = description;
        this.visibility = visibility;
        this.notes = notes;
        this.keywords = keywords;
        this.homepage = homepage;
        this.issues = issues;
        this.source = source;
        this.support = support;
        this.licenseName = licenseName;
        this.licenseUrl = licenseUrl;
        this.forumSync = forumSync;
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

    public Long getRecommendedVersionId() {
        return recommendedVersionId;
    }

    public void setRecommendedVersionId(Long recommendedVersionId) {
        this.recommendedVersionId = recommendedVersionId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getTopicId() {
        return topicId;
    }

    public long getPostId() {
        return postId;
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

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public JSONB getNotes() {
        return notes;
    }

    public void setNotes(JSONB notes) {
        this.notes = notes;
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
                ", recommendedVersionId=" + recommendedVersionId +
                ", ownerId=" + ownerId +
                ", topicId=" + topicId +
                ", postId=" + postId +
                ", category=" + category +
                ", description='" + description + '\'' +
                ", visibility=" + visibility +
                ", notes=" + notes +
                ", keywords=" + keywords +
                ", homepage='" + homepage + '\'' +
                ", issues='" + issues + '\'' +
                ", source='" + source + '\'' +
                ", support='" + support + '\'' +
                ", licenseName='" + licenseName + '\'' +
                ", licenseUrl='" + licenseUrl + '\'' +
                ", forumSync=" + forumSync +
                "} " + super.toString();
    }
}
