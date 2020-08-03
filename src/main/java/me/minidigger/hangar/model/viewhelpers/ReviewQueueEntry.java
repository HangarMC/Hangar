package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.Color;
import me.minidigger.hangar.model.generated.ProjectNamespace;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

public class ReviewQueueEntry {

    private ProjectNamespace namespace;
    private String projectName;
    private String versionString;
    private OffsetDateTime versionCreatedAt;
    private String channelName;
    private Color channelColor;
    private String versionAuthor;

    private Long reviewerId;
    private String reviewerName;
    private OffsetDateTime reviewStarted;
    private OffsetDateTime reviewEnded;

    public ReviewQueueEntry(ProjectNamespace namespace, String projectName, String versionString, OffsetDateTime versionCreatedAt, String channelName, Color channelColor, String versionAuthor, @Nullable Long reviewerId, @Nullable String reviewerName, @Nullable OffsetDateTime reviewStarted, @Nullable OffsetDateTime reviewEnded) {
        this.namespace = namespace;
        this.projectName = projectName;
        this.versionString = versionString;
        this.versionCreatedAt = versionCreatedAt;
        this.channelName = channelName;
        this.channelColor = channelColor;
        this.versionAuthor = versionAuthor;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewStarted = reviewStarted;
        this.reviewEnded = reviewEnded;
    }

    public ReviewQueueEntry(ProjectNamespace namespace, String projectName, String versionString, OffsetDateTime versionCreatedAt, String channelName, Color channelColor, String versionAuthor) {
        this(namespace, projectName, versionString, versionCreatedAt, channelName, channelColor, versionAuthor, null, null, null, null);
    }

    public boolean isUnfinished() {
        return reviewEnded == null;
    }

    public boolean hasReviewer() {
        return reviewerId != null;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersionString() {
        return versionString;
    }

    public OffsetDateTime getVersionCreatedAt() {
        return versionCreatedAt;
    }

    public String getChannelName() {
        return channelName;
    }

    public Color getChannelColor() {
        return channelColor;
    }

    public String getVersionAuthor() {
        return versionAuthor;
    }

    @Nullable
    public Long getReviewerId() {
        return reviewerId;
    }

    @Nullable
    public String getReviewerName() {
        return reviewerName;
    }

    @Nullable
    public OffsetDateTime getReviewStarted() {
        return reviewStarted;
    }

    @Nullable
    public OffsetDateTime getReviewEnded() {
        return reviewEnded;
    }
}
