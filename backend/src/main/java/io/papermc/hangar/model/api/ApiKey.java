package io.papermc.hangar.model.api;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ApiKey extends Model {

    private final String name;
    private final String tokenIdentifier;
    private final List<NamedPermission> permissions;
    private final @Nullable OffsetDateTime lastUsed;
    @Schema(description = "The time the key stops working, if it was created with an expiration date")
    private final @Nullable OffsetDateTime expiresAt;
    @Schema(description = "Whether the key may only be used on the projects listed below")
    private final boolean projectScoped;
    @Schema(description = "The projects the key is limited to, empty unless the key is project scoped")
    private final List<ProjectNamespace> projects;

    public ApiKey(final OffsetDateTime createdAt, final String name, final String tokenIdentifier, final Permission permissions, final @Nullable OffsetDateTime lastUsed, final @Nullable OffsetDateTime expiresAt, final boolean projectScoped, final @Nullable List<String> projects) {
        super(createdAt);
        this.name = name;
        this.tokenIdentifier = tokenIdentifier;
        this.permissions = permissions.toNamed();
        this.lastUsed = lastUsed;
        this.expiresAt = expiresAt;
        this.projectScoped = projectScoped;
        this.projects = projects == null ? List.of() : projects.stream().map(ProjectNamespace::new).toList();
    }

    public String getName() {
        return this.name;
    }

    public String getTokenIdentifier() {
        return this.tokenIdentifier;
    }

    public List<NamedPermission> getPermissions() {
        return this.permissions;
    }

    public @Nullable OffsetDateTime getLastUsed() {
        return this.lastUsed;
    }

    public @Nullable OffsetDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public boolean isProjectScoped() {
        return this.projectScoped;
    }

    public List<ProjectNamespace> getProjects() {
        return this.projects;
    }
}
