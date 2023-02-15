package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.ReviewAction;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jetbrains.annotations.Nullable;

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

    public HangarReviewQueueEntry(@Nested("pn") final ProjectNamespace namespace, final long versionId, final String versionString, final List<Platform> platforms, final OffsetDateTime versionCreatedAt, final String versionAuthor, final String channelName, @EnumByOrdinal final Color channelColor) {
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
        return this.namespace;
    }

    public long getVersionId() {
        return this.versionId;
    }

    public String getVersionString() {
        return this.versionString;
    }

    public List<Platform> getPlatforms() {
        return this.platforms;
    }

    public OffsetDateTime getVersionCreatedAt() {
        return this.versionCreatedAt;
    }

    public String getVersionAuthor() {
        return this.versionAuthor;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public Color getChannelColor() {
        return this.channelColor;
    }

    public List<Review> getReviews() {
        return this.reviews;
    }

    public record Review(String reviewerName, OffsetDateTime reviewStarted, @Nullable OffsetDateTime reviewEnded, @EnumByOrdinal ReviewAction lastAction) {
    }

    @Override
    public String toString() {
        return "HangarReviewQueueEntry{" +
            "namespace=" + this.namespace +
            ", versionId=" + this.versionId +
            ", versionString='" + this.versionString + '\'' +
            ", platforms=" + this.platforms +
            ", versionCreatedAt=" + this.versionCreatedAt +
            ", versionAuthor='" + this.versionAuthor + '\'' +
            ", channelName='" + this.channelName + '\'' +
            ", channelColor=" + this.channelColor +
            ", reviews=" + this.reviews +
            '}';
    }
}
