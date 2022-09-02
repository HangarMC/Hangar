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
import java.time.OffsetDateTime;
import java.util.function.Consumer;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionTable extends Table implements Named, ModelVisible, ProjectIdentified, VersionIdentified, Loggable<VersionContext> {

    private String versionString;
    private String description;
    private final long projectId;
    private long channelId;
    private Long reviewerId;
    private OffsetDateTime approvedAt;
    private final long authorId;
    private Visibility visibility = Visibility.PUBLIC;
    private ReviewState reviewState = ReviewState.UNREVIEWED;
    private boolean createForumPost;
    private Long postId;

    @JdbiConstructor
    public ProjectVersionTable(OffsetDateTime createdAt, long id, String versionString, String description, long projectId, long channelId, Long reviewerId, OffsetDateTime approvedAt, long authorId, @EnumByOrdinal Visibility visibility, @EnumByOrdinal ReviewState reviewState, boolean createForumPost, Long postId) {
        super(createdAt, id);
        this.versionString = versionString;
        this.description = description;
        this.projectId = projectId;
        this.channelId = channelId;
        this.reviewerId = reviewerId;
        this.approvedAt = approvedAt;
        this.authorId = authorId;
        this.visibility = visibility;
        this.reviewState = reviewState;
        this.createForumPost = createForumPost;
        this.postId = postId;
    }

    public ProjectVersionTable(String versionString, String description, long projectId, long channelId, long authorId, boolean createForumPost) {
        this.versionString = versionString;
        this.description = description;
        this.projectId = projectId;
        this.channelId = channelId;
        this.authorId = authorId;
        this.createForumPost = createForumPost;
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
            ", reviewerId=" + reviewerId +
            ", approvedAt=" + approvedAt +
            ", authorId=" + authorId +
            ", visibility=" + visibility +
            ", reviewState=" + reviewState +
            ", createForumPost=" + createForumPost +
            ", postId=" + postId +
            "} " + super.toString();
    }
}
