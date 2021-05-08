package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.api.project.version.PromotedVersion;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.List;

public class Project extends ProjectCompact {

    private final String description;
    private final OffsetDateTime lastUpdated;
    private final UserActions userActions;
    private final ProjectSettings settings;
    private final Long topicId;
    private final Long postId;
    protected final List<PromotedVersion> promotedVersions;

    @JdbiConstructor
    public Project(OffsetDateTime createdAt, String name, @Nested ProjectNamespace namespace, @Nested ProjectStats stats, @EnumByOrdinal Category category, String description, OffsetDateTime lastUpdated, @EnumByOrdinal Visibility visibility, @Nested UserActions userActions, @Nested ProjectSettings settings, Long topicId, Long postId, List<PromotedVersion> promotedVersions) {
        super(createdAt, name, namespace, stats, category, visibility);
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.userActions = userActions;
        this.settings = settings;
        this.topicId = topicId;
        this.postId = postId;
        this.promotedVersions = promotedVersions;
    }

    public Project(Project other) {
        super(other.createdAt, other.name, other.namespace, other.stats, other.category, other.visibility);
        this.description = other.description;
        this.lastUpdated = other.lastUpdated;
        this.userActions = other.userActions;
        this.settings = other.settings;
        this.topicId = other.topicId;
        this.postId = other.postId;
        this.promotedVersions = other.promotedVersions;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public UserActions getUserActions() {
        return userActions;
    }

    public ProjectSettings getSettings() {
        return settings;
    }

    public List<PromotedVersion> getPromotedVersions() {
        return promotedVersions;
    }

    public Long getTopicId() {
        return topicId;
    }

    public Long getPostId() {
        return postId;
    }

    @Override
    public String toString() {
        return "Project{" +
                "description='" + description + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", userActions=" + userActions +
                ", settings=" + settings +
                ", promotedVersions=" + promotedVersions +
                "} " + super.toString();
    }
}
