package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.modelold.generated.PlatformDependency;
import io.papermc.hangar.modelold.viewhelpers.VersionDependencies;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.List;

public class ProjectVersionTable extends Table implements Named, Visible {

    private final String versionString;
    private VersionDependencies dependencies;
    private String description;
    private final long projectId;
    private long channelId;
    private final Long fileSize;
    private final String hash;
    private final String fileName;
    private Long reviewerId;
    private OffsetDateTime approvedAt;
    private final long authorId;
    @EnumByOrdinal
    private Visibility visibility = Visibility.PUBLIC;
    @EnumByOrdinal
    private ReviewState reviewState = ReviewState.UNREVIEWED;
    private boolean createForumPost;
    private Long postId;
    private String externalUrl;
    private List<PlatformDependency> platforms;

    public ProjectVersionTable(String versionString, @Nested VersionDependencies dependencies, String description, long projectId, long channelId, Long fileSize, String hash, String fileName, long authorId, boolean createForumPost, String externalUrl, @Nested List<PlatformDependency> platforms) {
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

    @JdbiConstructor
    public ProjectVersionTable(OffsetDateTime createdAt, long id, String versionString, VersionDependencies dependencies, String description, long projectId, long channelId, Long fileSize, String hash, String fileName, Long reviewerId, OffsetDateTime approvedAt, long authorId, @EnumByOrdinal Visibility visibility, @EnumByOrdinal ReviewState reviewState, boolean createForumPost, Long postId, String externalUrl, List<PlatformDependency> platforms) {
        super(createdAt, id);
        this.versionString = versionString;
        this.dependencies = dependencies;
        this.description = description;
        this.projectId = projectId;
        this.channelId = channelId;
        this.fileSize = fileSize;
        this.hash = hash;
        this.fileName = fileName;
        this.reviewerId = reviewerId;
        this.approvedAt = approvedAt;
        this.authorId = authorId;
        this.visibility = visibility;
        this.reviewState = reviewState;
        this.createForumPost = createForumPost;
        this.postId = postId;
        this.externalUrl = externalUrl;
        this.platforms = platforms;
    }

    public String getVersionString() {
        return versionString;
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

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getHash() {
        return hash;
    }

    public String getFileName() {
        return fileName;
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

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public ReviewState getReviewState() {
        return reviewState;
    }

    public void setReviewState(ReviewState reviewState) {
        this.reviewState = reviewState;
    }

    public boolean isCreateForumPost() {
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

    @Override
    public String getName() {
        return this.versionString;
    }
}
