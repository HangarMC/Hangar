package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.ModelVisible;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.identified.ProjectIdentified;
import io.papermc.hangar.model.identified.VersionIdentified;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.loggable.Loggable;
import io.papermc.hangar.service.internal.UserActionLogService;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

public class ProjectVersionTable extends Table implements Named, ModelVisible, ProjectIdentified, VersionIdentified, Loggable<VersionContext> {

    private String versionString;
    private String description;
    private final long projectId;
    private long channelId;
    private final Long fileSize;
    private final String hash;
    private final String fileName;
    private Long reviewerId;
    private OffsetDateTime approvedAt;
    private final long authorId;
    private Visibility visibility = Visibility.PUBLIC;
    private ReviewState reviewState = ReviewState.UNREVIEWED;
    private boolean createForumPost;
    private Long postId;
    private String externalUrl;

    @JdbiConstructor
    public ProjectVersionTable(OffsetDateTime createdAt, long id, String versionString, String description, long projectId, long channelId, Long fileSize, String hash, String fileName, Long reviewerId, OffsetDateTime approvedAt, long authorId, @EnumByOrdinal Visibility visibility, @EnumByOrdinal ReviewState reviewState, boolean createForumPost, Long postId, String externalUrl) {
        super(createdAt, id);
        this.versionString = versionString;
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
    }

    public ProjectVersionTable(String versionString, String description, long projectId, long channelId, Long fileSize, String hash, String fileName, long authorId, boolean createForumPost, String externalUrl) {
        this.versionString = versionString;
        this.description = description;
        this.projectId = projectId;
        this.channelId = channelId;
        this.fileSize = fileSize;
        this.hash = hash;
        this.fileName = fileName;
        this.authorId = authorId;
        this.createForumPost = createForumPost;
        this.externalUrl = externalUrl;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(final String versionString) {
        this.versionString = versionString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
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
    @EnumByOrdinal
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    @EnumByOrdinal
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

    @Override
    public String getName() {
        return this.versionString;
    }

    @Override
    public long getVersionId() {
        return id;
    }

    @Override
    public Consumer<LoggedAction<VersionContext>> getLogInserter(UserActionLogService actionLogger) {
        return actionLogger::version;
    }

    @Override
    public VersionContext createLogContext() {
        return VersionContext.of(this.projectId, this.id);
    }

    @Override
    public String toString() {
        return "ProjectVersionTable{" +
                "versionString='" + versionString + '\'' +
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
                "} " + super.toString();
    }
}
