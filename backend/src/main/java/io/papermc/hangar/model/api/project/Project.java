package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.api.project.settings.ProjectSettings;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.Nullable;

public class Project extends ProjectCompact {

    @Schema(description = "The short description of the project")
    private final String description;
    @Schema(description = "Information about your interactions with the project")
    private final UserActions userActions;
    @Schema(description = "The settings of the project")
    private final ProjectSettings settings;

    @JdbiConstructor
    public Project(final OffsetDateTime createdAt, final long id, final String name, @Nested final ProjectNamespace namespace, @Nested final ProjectStats stats, @EnumByOrdinal final Category category, final String description, final OffsetDateTime lastUpdated, @EnumByOrdinal final Visibility visibility, @Nested final UserActions userActions, @Nested final ProjectSettings settings) {
        super(createdAt, id, name, namespace, stats, category, lastUpdated, visibility);
        this.description = description;
        this.userActions = userActions;
        this.settings = settings;
    }

    public Project(final Project other) {
        super(other.createdAt, other.id, other.name, other.namespace, other.stats, other.category, other.lastUpdated, other.visibility);
        this.description = other.description;
        this.userActions = other.userActions;
        this.settings = other.settings;
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
