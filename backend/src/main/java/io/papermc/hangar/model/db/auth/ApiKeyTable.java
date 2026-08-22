package io.papermc.hangar.model.db.auth;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ApiKeyTable extends Table implements Named {

    private final String name;
    private final long ownerId;
    private final UUID tokenIdentifier;
    private final String token;
    private final Permission permissions;
    private final @Nullable OffsetDateTime expiresAt;
    private final boolean projectScoped;
    private final Set<Long> scopedProjectIds;
    private final Set<String> scopedProjectSlugs;
    private OffsetDateTime lastUsed = null;

    public ApiKeyTable(final String name, final long ownerId, final UUID tokenIdentifier, final String token, final Permission permissions, final @Nullable OffsetDateTime expiresAt, final boolean projectScoped) {
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permissions = permissions;
        this.expiresAt = expiresAt;
        this.projectScoped = projectScoped;
        this.scopedProjectIds = Set.of();
        this.scopedProjectSlugs = Set.of();
    }

    @JdbiConstructor
    public ApiKeyTable(final OffsetDateTime createdAt, final long id, final String name, final long ownerId, final UUID tokenIdentifier, final String token, final Permission permissions, final OffsetDateTime lastUsed, final @Nullable OffsetDateTime expiresAt, final boolean projectScoped, final @Nullable List<Long> scopedProjectIds, final @Nullable List<String> scopedProjectSlugs) {
        super(createdAt, id);
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permissions = permissions;
        this.lastUsed = lastUsed;
        this.expiresAt = expiresAt;
        this.projectScoped = projectScoped;
        this.scopedProjectIds = scopedProjectIds == null ? Set.of() : Set.copyOf(scopedProjectIds);
        this.scopedProjectSlugs = scopedProjectSlugs == null ? Set.of() : scopedProjectSlugs.stream().map(slug -> slug.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getName() {
        return this.name;
    }

    public long getOwnerId() {
        return this.ownerId;
    }

    public UUID getTokenIdentifier() {
        return this.tokenIdentifier;
    }

    public String getToken() {
        return this.token;
    }

    public Permission getPermissions() {
        return this.permissions;
    }

    public OffsetDateTime getLastUsed() {
        return this.lastUsed;
    }

    public void setLastUsed(final OffsetDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }

    public @Nullable OffsetDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public boolean isExpired() {
        return this.expiresAt != null && this.expiresAt.isBefore(OffsetDateTime.now());
    }

    public boolean isProjectScoped() {
        return this.projectScoped;
    }

    public Set<Long> getScopedProjectIds() {
        return this.scopedProjectIds;
    }

    public Set<String> getScopedProjectSlugs() {
        return this.scopedProjectSlugs;
    }

    public boolean coversProject(final long projectId) {
        return !this.projectScoped || this.scopedProjectIds.contains(projectId);
    }

    public boolean coversProject(final String slug) {
        return !this.projectScoped || this.scopedProjectSlugs.contains(slug.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return "ApiKeyTable{" +
            "name='" + this.name + '\'' +
            ", ownerId=" + this.ownerId +
            ", tokenIdentifier='" + this.tokenIdentifier + '\'' +
            ", token='" + this.token + '\'' +
            ", permissions=" + this.permissions +
            ", lastUsed=" + this.lastUsed +
            ", expiresAt=" + this.expiresAt +
            ", projectScoped=" + this.projectScoped +
            ", scopedProjectIds=" + this.scopedProjectIds +
            "} " + super.toString();
    }
}
