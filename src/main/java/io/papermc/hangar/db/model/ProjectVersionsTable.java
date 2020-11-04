package io.papermc.hangar.db.model;


import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.PlatformDependency;
import io.papermc.hangar.model.generated.ReviewState;
import io.papermc.hangar.model.viewhelpers.VersionDependencies;
import org.jdbi.v3.core.annotation.Unmappable;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;
import java.util.List;

public class ProjectVersionsTable {

    private long id;
    private OffsetDateTime createdAt;
    private String versionString;
    private VersionDependencies dependencies;
    private String description;
    private long projectId;
    private long channelId;
    private Long fileSize;
    private String hash;
    private String fileName;
    private Long reviewerId;
    private OffsetDateTime approvedAt;
    private long authorId;
    private Visibility visibility = Visibility.PUBLIC;
    private ReviewState reviewState = ReviewState.UNREVIEWED;
    private boolean createForumPost = true;
    private Long postId;
    private String externalUrl;
    private List<PlatformDependency> platforms;

    public ProjectVersionsTable(String versionString, VersionDependencies dependencies, String description, long projectId, long channelId, Long fileSize, String hash, String fileName, long authorId, boolean createForumPost, String externalUrl, List<PlatformDependency> platforms) {
        this.versionString = versionString;
        this.dependencies = dependencies;
        this.description = description;
        this.projectId = projectId;
        this.channelId = channelId;
        this.fileSize = fileSize;
        this.hash = hash;
        this.fileName = fileName;
        this.authorId = authorId;
        this.createForumPost = createForumPost;
        this.externalUrl = externalUrl;
        this.platforms = platforms;
    }

    public ProjectVersionsTable() { }

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


    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }


    public VersionDependencies getDependencies() {
        return dependencies;
    }

    public void setDependencies(VersionDependencies dependencies) {
        this.dependencies = dependencies;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }


    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }


    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }


    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }


    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }


    @EnumByOrdinal
    public Visibility getVisibility() {
        return visibility;
    }

    @EnumByOrdinal
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }


    @EnumByOrdinal
    public ReviewState getReviewState() {
        return reviewState;
    }

    @EnumByOrdinal
    public void setReviewState(ReviewState reviewState) {
        this.reviewState = reviewState;
    }


    public boolean getCreateForumPost() {
        return createForumPost;
    }

    public void setCreateForumPost(boolean createForumPost) {
        this.createForumPost = createForumPost;
    }


    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }


    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }


    public List<PlatformDependency> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<PlatformDependency> platforms) {
        this.platforms = platforms;
    }

    @Unmappable
    public boolean isExternal() {
        return this.externalUrl != null && this.fileName == null;
    }

    @Unmappable
    public String getVersionStringUrl() {
        return this.versionString + "." + this.id;
    }

    @Override
    public String toString() {
        return "ProjectVersionsTable{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", versionString='" + versionString + '\'' +
                ", description='" + description + '\'' +
                ", projectId=" + projectId +
                ", channelId=" + channelId +
                ", fileSize=" + fileSize +
                ", hash='" + hash + '\'' +
                ", fileName='" + fileName + '\'' +
                ", reviewerId=" + reviewerId +
                ", approvedAt=" + approvedAt +
                ", authorId=" + authorId +
                ", visibility=" + visibility +
                ", reviewState=" + reviewState +
                ", createForumPost=" + createForumPost +
                ", postId=" + postId +
                ", externalUrl='" + externalUrl + '\'' +
                ", dependencies=" + dependencies +
                ", platformDependencies=" + platforms +
                '}';
    }
}
