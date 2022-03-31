package io.papermc.hangar.db.model;


import io.papermc.hangar.controller.forms.NewProjectForm;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.annotation.Unmappable;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;
import java.util.Collection;

public class ProjectsTable implements Visitable, VisibilityModel {

    private long id;
    private OffsetDateTime createdAt;
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

    public ProjectsTable() { }

    public ProjectsTable(ProjectOwner projectOwner, Category category, NewProjectForm newProjectForm, Collection<String> keywords) {
        this.name = StringUtils.compact(newProjectForm.getName());
        this.slug = StringUtils.slugify(newProjectForm.getName());
        this.ownerName = projectOwner.getName();
        this.ownerId = projectOwner.getUserId();
        this.category = category;
        this.description = newProjectForm.getDescription();
        this.visibility = Visibility.NEW;
        this.homepage = StringUtils.stringOrNull(newProjectForm.getHomepageUrl());
        this.issues = StringUtils.stringOrNull(newProjectForm.getIssueTrackerUrl());
        this.source = StringUtils.stringOrNull(newProjectForm.getSourceUrl());
        this.support = StringUtils.stringOrNull(newProjectForm.getExternalSupportUrl());
        this.keywords = keywords;
        this.licenseName = StringUtils.stringOrNull("custom".equalsIgnoreCase(newProjectForm.getLicenseType()) ? newProjectForm.getLicenseName() : newProjectForm.getLicenseType());
        this.licenseUrl = StringUtils.stringOrNull(newProjectForm.getLicenseUrl());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }


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

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }


    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }


    @EnumByOrdinal
    public Category getCategory() {
        return category;
    }

    @EnumByOrdinal
    public void setCategory(Category category) {
        this.category = category;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @EnumByOrdinal
    public Visibility getVisibility() {
        return visibility;
    }

    @EnumByOrdinal
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


    public boolean getForumSync() {
        return forumSync;
    }

    public void setForumSync(boolean forumSync) {
        this.forumSync = forumSync;
    }

    @Unmappable
    @Override
    public String getUrl() {
        return "/" + getOwnerName() + "/" + getSlug();
    }
}
