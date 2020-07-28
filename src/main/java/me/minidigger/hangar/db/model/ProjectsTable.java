package me.minidigger.hangar.db.model;


import me.minidigger.hangar.model.Category;
import me.minidigger.hangar.model.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.OffsetDateTime;

public class ProjectsTable {

    private long id;
    private OffsetDateTime createdAt;
    private String pluginId;
    private String name;
    private String slug;
    private String ownerName;
    private long recommendedVersionId;
    private long ownerId;
    private long topicId;
    private long postId;
    private Category category;
    private String description;
    private Visibility visibility;
    private Object notes; // TODO jsonb
    private String keywords;
    private String homepage;
    private String issues;
    private String source;
    private String support;
    private String licenseName;
    private String licenseUrl;
    private boolean forumSync;

    public ProjectsTable() {
        //
    }

    public ProjectsTable(String pluginId, String name, String slug, String ownerName, long ownerId, Category category, String description, Visibility visibility) {
        this.pluginId = pluginId;
        this.name = name;
        this.slug = slug;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.category = category;
        this.description = description;
        this.visibility = visibility;
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


    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
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


    public long getRecommendedVersionId() {
        return recommendedVersionId;
    }

    public void setRecommendedVersionId(long recommendedVersionId) {
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


    public Object getNotes() { //TODO jsonb
        return notes;
    }

    public void setNotes(Object notes) {
        this.notes = notes;
    }


    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
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

}
