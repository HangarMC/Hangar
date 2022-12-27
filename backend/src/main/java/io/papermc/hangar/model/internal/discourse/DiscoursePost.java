package io.papermc.hangar.model.internal.discourse;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class DiscoursePost {

    private long id;
    @JsonProperty("topic_id")
    private long topicId;
    @JsonProperty("user_id")
    private long userId;
    private String username;
    @JsonProperty("topic_slug")
    private String topicSlug;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
    @JsonProperty("deleted_at")
    private @Nullable OffsetDateTime deletedAt;
    @JsonProperty("cooked")
    private String content;
    @JsonProperty("reply_count")
    private long replyCount;
    @JsonProperty("post_number")
    private long postNumber;

    public DiscoursePost() {
        //
    }

    public DiscoursePost(final long id, final long topicId, final long userId, final String username, final String topicSlug, final OffsetDateTime createdAt, final OffsetDateTime updatedAt, final @Nullable OffsetDateTime deletedAt, final String content, final long replyCount, final long postNumber) {
        this.id = id;
        this.topicId = topicId;
        this.userId = userId;
        this.username = username;
        this.topicSlug = topicSlug;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.content = content;
        this.replyCount = replyCount;
        this.postNumber = postNumber;
    }

    public boolean isTopic() {
        return this.postNumber == 1;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public long getId() {
        return this.id;
    }

    public long getTopicId() {
        return this.topicId;
    }

    public long getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public String getTopicSlug() {
        return this.topicSlug;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return this.deletedAt;
    }

    public String getContent() {
        return this.content;
    }

    public long getReplyCount() {
        return this.replyCount;
    }

    public long getPostNumber() {
        return this.postNumber;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final DiscoursePost that = (DiscoursePost) o;
        return this.id == that.id && this.topicId == that.topicId && this.userId == that.userId && this.replyCount == that.replyCount && this.postNumber == that.postNumber && Objects.equals(this.username, that.username) && Objects.equals(this.topicSlug, that.topicSlug) && Objects.equals(this.createdAt, that.createdAt) && Objects.equals(this.updatedAt, that.updatedAt) && Objects.equals(this.deletedAt, that.deletedAt) && Objects.equals(this.content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.topicId, this.userId, this.username, this.topicSlug, this.createdAt, this.updatedAt, this.deletedAt, this.content, this.replyCount, this.postNumber);
    }

    @Override
    public String toString() {
        return "DiscoursePost{" +
            "id=" + this.id +
            ", topicId=" + this.topicId +
            ", userId=" + this.userId +
            ", username='" + this.username + '\'' +
            ", topicSlug='" + this.topicSlug + '\'' +
            ", createdAt=" + this.createdAt +
            ", updatedAt=" + this.updatedAt +
            ", deletedAt=" + this.deletedAt +
            ", content='" + this.content + '\'' +
            ", replyCount=" + this.replyCount +
            ", postNumber=" + this.postNumber +
            '}';
    }
}
