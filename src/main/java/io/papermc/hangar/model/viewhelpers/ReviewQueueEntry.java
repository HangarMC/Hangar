package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.Color;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

public class ReviewQueueEntry {

    private String projectAuthor;
    private String projectName;
    private String projectSlug;
    private String versionString;
    private String versionStringUrl;
    private OffsetDateTime versionCreatedAt;
    private String channelName;
    private Color channelColor;
    private String versionAuthor;

    private Long reviewerId;
    private String reviewerName;
    private OffsetDateTime reviewStarted;
    private OffsetDateTime reviewEnded;

    public ReviewQueueEntry() {
    }

    public ReviewQueueEntry(String projectAuthor, String projectName, String projectSlug, String versionString, String versionStringUrl, OffsetDateTime versionCreatedAt, String channelName, Color channelColor, String versionAuthor, @Nullable Long reviewerId, @Nullable String reviewerName, @Nullable OffsetDateTime reviewStarted, @Nullable OffsetDateTime reviewEnded) {
        this.projectAuthor = projectAuthor;
        this.projectName = projectName;
        this.projectSlug = projectSlug;
        this.versionString = versionString;
        this.versionStringUrl = versionStringUrl;
        this.versionCreatedAt = versionCreatedAt;
        this.channelName = channelName;
        this.channelColor = channelColor;
        this.versionAuthor = versionAuthor;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewStarted = reviewStarted;
        this.reviewEnded = reviewEnded;
    }

    public boolean isUnfinished() {
        return reviewEnded == null;
    }

    public boolean hasReviewer() {
        return reviewerId != null;
    }

    public String getNamespace() {
        return projectAuthor + "/" + projectSlug;
    }

    public String getAuthor() {
        return projectAuthor;
    }

    public String getProjectAuthor() {
        return projectAuthor;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getSlug() {
        return projectSlug;
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public String getVersionString() {
        return versionString;
    }

    public String getVersionStringUrl() {
        return versionStringUrl;
    }

    public OffsetDateTime getVersionCreatedAt() {
        return versionCreatedAt;
    }

    public String getChannelName() {
        return channelName;
    }

    @EnumByOrdinal
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

    public void setProjectAuthor(String author) {
        this.projectAuthor = author;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    public void setVersionStringUrl(String versionStringUrl) {
        this.versionStringUrl = versionStringUrl;
    }

    public void setVersionCreatedAt(OffsetDateTime versionCreatedAt) {
        this.versionCreatedAt = versionCreatedAt;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    @EnumByOrdinal
    public void setChannelColor(Color channelColor) {
        this.channelColor = channelColor;
    }

    public void setVersionAuthor(String versionAuthor) {
        this.versionAuthor = versionAuthor;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public void setReviewStarted(OffsetDateTime reviewStarted) {
        this.reviewStarted = reviewStarted;
    }

    public void setReviewEnded(OffsetDateTime reviewEnded) {
        this.reviewEnded = reviewEnded;
    }

    @Override
    public String toString() {
        return "ReviewQueueEntry{" +
                "projectAuthor='" + projectAuthor + '\'' +
                ", projectName='" + projectName + '\'' +
                ", projectSlug='" + projectSlug + '\'' +
                ", versionString='" + versionString + '\'' +
                ", versionStringUrl='" + versionStringUrl + '\'' +
                ", versionCreatedAt=" + versionCreatedAt +
                ", channelName='" + channelName + '\'' +
                ", channelColor=" + channelColor +
                ", versionAuthor='" + versionAuthor + '\'' +
                ", reviewerId=" + reviewerId +
                ", reviewerName='" + reviewerName + '\'' +
                ", reviewStarted=" + reviewStarted +
                ", reviewEnded=" + reviewEnded +
                '}';
    }
}
