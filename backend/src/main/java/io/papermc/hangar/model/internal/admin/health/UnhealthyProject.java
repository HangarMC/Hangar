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

    public UnhealthyProject(@Nested ProjectNamespace namespace, Long topicId, Long postId, OffsetDateTime lastUpdated, @EnumByOrdinal Visibility visibility) {
        this.namespace = namespace;
        this.topicId = topicId;
        this.postId = postId;
        this.lastUpdated = lastUpdated;
        this.visibility = visibility;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public Long getTopicId() {
        return topicId;
    }

    public Long getPostId() {
        return postId;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "UnhealthyProject{" +
                "namespace=" + namespace +
                ", topicId=" + topicId +
                ", postId=" + postId +
                ", lastUpdated=" + lastUpdated +
                ", visibility=" + visibility +
                "} " + super.toString();
    }
}
