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

    @JdbiConstructor
    public ProjectVersionTable(final OffsetDateTime createdAt, final long id, final String versionString, final String description, final long projectId, final long channelId, final Long reviewerId, final OffsetDateTime approvedAt, final long authorId, @EnumByOrdinal final Visibility visibility, @EnumByOrdinal final ReviewState reviewState) {
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
    }

    public ProjectVersionTable(final String versionString, final String description, final long projectId, final long channelId, final long authorId) {
        this.versionString = versionString;
        this.description = description;
        this.projectId = projectId;
        this.channelId = channelId;
        this.authorId = authorId;
    }

    public String getVersionString() {
        return this.versionString;
    }

    public void setVersionString(final String versionString) {
        this.versionString = versionString;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public long getProjectId() {
        return this.projectId;
    }

    public long getChannelId() {
        return this.channelId;
    }

    public void setChannelId(final long channelId) {
        this.channelId = channelId;
    }

    public Long getReviewerId() {
        return this.reviewerId;
    }

    public void setReviewerId(final Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public OffsetDateTime getApprovedAt() {
        return this.approvedAt;
    }

    public void setApprovedAt(final OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public long getAuthorId() {
        return this.authorId;
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

    @EnumByOrdinal
    public ReviewState getReviewState() {
        return this.reviewState;
    }

    public void setReviewState(final ReviewState reviewState) {
        this.reviewState = reviewState;
    }

    @Override
    public String getName() {
        return this.versionString;
    }

    @Override
    public long getVersionId() {
        return this.id;
    }

    @Override
    public Consumer<LoggedAction<VersionContext>> getLogInserter(final UserActionLogService actionLogger) {
        return actionLogger::version;
    }

    @Override
    public VersionContext createLogContext() {
        return VersionContext.of(this.projectId, this.id);
    }

    @Override
    public String toString() {
        return "ProjectVersionTable{" +
            "versionString='" + this.versionString + '\'' +
            ", description='" + this.description + '\'' +
            ", projectId=" + this.projectId +
            ", channelId=" + this.channelId +
            ", reviewerId=" + this.reviewerId +
            ", approvedAt=" + this.approvedAt +
            ", authorId=" + this.authorId +
            ", visibility=" + this.visibility +
            ", reviewState=" + this.reviewState +
            "} " + super.toString();
    }
}
