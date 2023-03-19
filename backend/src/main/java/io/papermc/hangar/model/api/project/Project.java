package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.api.project.settings.ProjectSettings;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.Nullable;

public class Project extends ProjectCompact {

    private final String description;
    private final UserActions userActions;
    private final ProjectSettings settings;
    private final Long topicId;
    private final Long postId;

    @JdbiConstructor
    public Project(final OffsetDateTime createdAt, final long id, final String name, @Nested final ProjectNamespace namespace, @Nested final ProjectStats stats, @EnumByOrdinal final Category category, final String description, final OffsetDateTime lastUpdated, @EnumByOrdinal final Visibility visibility, @Nested final UserActions userActions, @Nested final ProjectSettings settings, final Long topicId, final Long postId) {
        super(createdAt, id, name, namespace, stats, category, lastUpdated, visibility);
        this.description = description;
        this.userActions = userActions;
        this.settings = settings;
        this.topicId = topicId;
        this.postId = postId;
    }

    public Project(final Project other) {
        super(other.createdAt, other.id, other.name, other.namespace, other.stats, other.category, other.lastUpdated, other.visibility);
        this.description = other.description;
        this.userActions = other.userActions;
        this.settings = other.settings;
        this.topicId = other.topicId;
        this.postId = other.postId;
    }

    public String getDescription() {
        return this.description;
    }

    public UserActions getUserActions() {
        return this.userActions;
    }

    public ProjectSettings getSettings() {
        return this.settings;
    }

    public @Nullable Long getTopicId() {
        return this.topicId;
    }

    public @Nullable Long getPostId() {
        return this.postId;
    }

    @Override
    public String toString() {
        return "Project{" +
            "description='" + this.description + '\'' +
            ", lastUpdated=" + this.lastUpdated +
            ", userActions=" + this.userActions +
            ", settings=" + this.settings +
            "} " + super.toString();
    }
}
