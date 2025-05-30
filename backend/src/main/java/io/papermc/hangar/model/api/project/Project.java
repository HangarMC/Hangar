package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.api.project.settings.ProjectSettings;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class Project extends ProjectCompact {

    @Schema(description = "Information about your interactions with the project")
    private final UserActions userActions;
    @Schema(description = "The settings of the project")
    private final ProjectSettings settings;
    @Schema(description = "The platforms and versions the project supports")
    private final Map<Platform, SortedSet<String>> supportedPlatforms;
    @Schema(description = "The content of the main page")
    private final String mainPageContent;
    @Schema(description = "The names of the members of the project")
    private final List<String> memberNames;

    @JsonCreator
    @JdbiConstructor
    public Project(final OffsetDateTime createdAt, final long id, final String name, @Nested final ProjectNamespace namespace, @Nested final ProjectStats stats, @EnumByOrdinal final Category category, final String description, final OffsetDateTime lastUpdated, @EnumByOrdinal final Visibility visibility, @Nested @Nullable final UserActions userActions, @Nested final ProjectSettings settings, final String avatar, final String avatarFallback, final Map<Platform, SortedSet<String>> supportedPlatforms, @Nullable final String mainPageContent, @Nullable final List<String> memberNames) {
        super(createdAt, id, name, namespace, description, stats, category, lastUpdated, visibility, avatar, avatarFallback);
        this.userActions = userActions;
        this.settings = settings;
        this.supportedPlatforms = supportedPlatforms;
        this.mainPageContent = mainPageContent;
        this.memberNames = memberNames;
    }

    public Project(final Project other) {
        super(other.createdAt, other.id, other.name, other.namespace, other.description, other.stats, other.category, other.lastUpdated, other.visibility, null, null);
        this.userActions = other.userActions;
        this.settings = other.settings;
        this.mainPageContent = other.mainPageContent;
        this.memberNames = other.memberNames;
        this.avatarUrl = other.avatarUrl;
        this.supportedPlatforms = other.supportedPlatforms;
    }

    public UserActions getUserActions() {
        return this.userActions;
    }

    public ProjectSettings getSettings() {
        return this.settings;
    }

    public Map<Platform, SortedSet<String>> getSupportedPlatforms() {
        return supportedPlatforms;
    }

    public String getMainPageContent() {
        return mainPageContent;
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    @Override
    public String toString() {
        return "Project{" +
            ", lastUpdated=" + this.lastUpdated +
            ", userActions=" + this.userActions +
            ", settings=" + this.settings +
            "} " + super.toString();
    }
}
