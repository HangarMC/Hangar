package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.ReviewAction;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class HangarReviewQueueEntry {

    private final ProjectNamespace namespace;
    private final long versionId;
    private final String versionString;
    private final List<Platform> platforms;
    private final OffsetDateTime versionCreatedAt;
    private final String versionAuthor;
    private final String channelName;
    private final Color channelColor;
    private final List<Review> reviews;

    public HangarReviewQueueEntry(@Nested("pn") ProjectNamespace namespace, long versionId, String versionString, List<Platform> platforms, OffsetDateTime versionCreatedAt, String versionAuthor, String channelName, @EnumByOrdinal Color channelColor) {
        this.namespace = namespace;
        this.versionId = versionId;
        this.versionString = versionString;
        this.platforms = platforms;
        this.versionCreatedAt = versionCreatedAt;
        this.versionAuthor = versionAuthor;
        this.channelName = channelName;
        this.channelColor = channelColor;
        this.reviews = new ArrayList<>();
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public long getVersionId() {
        return versionId;
    }

    public String getVersionString() {
        return versionString;
    }

    public List<Platform> getPlatforms() {
        return platforms;
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

    public List<Review> getReviews() {
        return reviews;
    }

    public static class Review {

        private final String reviewerName;
        private final OffsetDateTime reviewStarted;
        private final OffsetDateTime reviewEnded;
        private final ReviewAction lastAction;

        public Review(String reviewerName, OffsetDateTime reviewStarted, @Nullable OffsetDateTime reviewEnded, @EnumByOrdinal ReviewAction lastAction) {
            this.reviewerName = reviewerName;
            this.reviewStarted = reviewStarted;
            this.reviewEnded = reviewEnded;
            this.lastAction = lastAction;
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

        public ReviewAction getLastAction() {
            return lastAction;
        }

        @Override
        public String toString() {
            return "Review{" +
                    "reviewerName='" + reviewerName + '\'' +
                    ", reviewStarted=" + reviewStarted +
                    ", reviewEnded=" + reviewEnded +
                    ", lastAction=" + lastAction +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "HangarReviewQueueEntry{" +
                "namespace=" + namespace +
                ", versionId=" + versionId +
                ", versionString='" + versionString + '\'' +
                ", platforms=" + platforms +
                ", versionCreatedAt=" + versionCreatedAt +
                ", versionAuthor='" + versionAuthor + '\'' +
                ", channelName='" + channelName + '\'' +
                ", channelColor=" + channelColor +
                ", reviews=" + reviews +
                '}';
    }
}
