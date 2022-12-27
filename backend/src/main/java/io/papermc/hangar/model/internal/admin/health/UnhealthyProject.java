package io.papermc.hangar.model.internal.admin.health;

import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;

public class UnhealthyProject implements Visible {

    private final ProjectNamespace namespace;
    private final Long topicId;
    private final Long postId;
    private final OffsetDateTime lastUpdated;
    private final Visibility visibility;

    public UnhealthyProject(@Nested final ProjectNamespace namespace, final Long topicId, final Long postId, final OffsetDateTime lastUpdated, @EnumByOrdinal final Visibility visibility) {
        this.namespace = namespace;
        this.topicId = topicId;
        this.postId = postId;
        this.lastUpdated = lastUpdated;
        this.visibility = visibility;
    }

    public ProjectNamespace getNamespace() {
        return this.namespace;
    }

    public Long getTopicId() {
        return this.topicId;
    }

    public Long getPostId() {
        return this.postId;
    }

    public OffsetDateTime getLastUpdated() {
        return this.lastUpdated;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public String toString() {
        return "UnhealthyProject{" +
                "namespace=" + this.namespace +
                ", topicId=" + this.topicId +
                ", postId=" + this.postId +
                ", lastUpdated=" + this.lastUpdated +
                ", visibility=" + this.visibility +
                "} " + super.toString();
    }
}
