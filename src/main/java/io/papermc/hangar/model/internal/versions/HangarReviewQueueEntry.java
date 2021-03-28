package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Color;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

public class HangarReviewQueueEntry {

    private final ProjectNamespace namespace;

    private final String versionString;
    // platform
    private final OffsetDateTime versionCreatedAt;
    private final String versionAuthor;

    private final String channelName;
    private final Color channelColor;

    private final String reviewerName;
    private final OffsetDateTime reviewStarted;
    private final OffsetDateTime reviewEnded;

    public HangarReviewQueueEntry(@Nested("pn") ProjectNamespace namespace, String versionString, OffsetDateTime versionCreatedAt, String versionAuthor, String channelName, @EnumByOrdinal Color channelColor, @Nullable String reviewerName, @Nullable OffsetDateTime reviewStarted, @Nullable OffsetDateTime reviewEnded) {
        this.namespace = namespace;
        this.versionString = versionString;
        this.versionCreatedAt = versionCreatedAt;
        this.versionAuthor = versionAuthor;
        this.channelName = channelName;
        this.channelColor = channelColor;
        this.reviewerName = reviewerName;
        this.reviewStarted = reviewStarted;
        this.reviewEnded = reviewEnded;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public String getVersionString() {
        return versionString;
    }

    public OffsetDateTime getVersionCreatedAt() {
        return versionCreatedAt;
    }

    public String getVersionAuthor() {
        return versionAuthor;
    }

    public String getChannelName() {
        return channelName;
    }

    public Color getChannelColor() {
        return channelColor;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public OffsetDateTime getReviewStarted() {
        return reviewStarted;
    }

    public OffsetDateTime getReviewEnded() {
        return reviewEnded;
    }

    @Override
    public String toString() {
        return "HangarReviewQueueEntry{" +
                "namespace=" + namespace +
                ", versionString='" + versionString + '\'' +
                ", versionCreatedAt=" + versionCreatedAt +
                ", versionAuthor='" + versionAuthor + '\'' +
                ", channelName='" + channelName + '\'' +
                ", channelColor=" + channelColor +
                ", reviewerName='" + reviewerName + '\'' +
                ", reviewStarted=" + reviewStarted +
                ", reviewEnded=" + reviewEnded +
                '}';
    }
}
