package io.papermc.hangar.model.internal.discourse;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Objects;

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
    @Nullable
    @JsonProperty("deleted_at")
    private OffsetDateTime deletedAt;
    @JsonProperty("cooked")
    private String content;
    @JsonProperty("reply_count")
    private long replyCount;
    @JsonProperty("post_number")
    private long postNumber;

    public DiscoursePost() {
        //
    }

    public DiscoursePost(long id, long topicId, long userId, String username, String topicSlug, OffsetDateTime createdAt, OffsetDateTime updatedAt, @Nullable OffsetDateTime deletedAt, String content, long replyCount, long postNumber) {
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
        return postNumber == 1;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public long getId() {
        return id;
    }

    public long getTopicId() {
        return topicId;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getTopicSlug() {
        return topicSlug;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public String getContent() {
        return content;
    }

    public long getReplyCount() {
        return replyCount;
    }

    public long getPostNumber() {
        return postNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoursePost that = (DiscoursePost) o;
        return id == that.id && topicId == that.topicId && userId == that.userId && replyCount == that.replyCount && postNumber == that.postNumber && Objects.equals(username, that.username) && Objects.equals(topicSlug, that.topicSlug) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(deletedAt, that.deletedAt) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, topicId, userId, username, topicSlug, createdAt, updatedAt, deletedAt, content, replyCount, postNumber);
    }

    @Override
    public String toString() {
        return "DiscoursePost{" +
               "id=" + id +
               ", topicId=" + topicId +
               ", userId=" + userId +
               ", username='" + username + '\'' +
               ", topicSlug='" + topicSlug + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               ", deletedAt=" + deletedAt +
               ", content='" + content + '\'' +
               ", replyCount=" + replyCount +
               ", postNumber=" + postNumber +
               '}';
    }
}
