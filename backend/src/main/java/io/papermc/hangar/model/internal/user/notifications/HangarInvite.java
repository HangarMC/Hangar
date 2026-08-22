package io.papermc.hangar.model.internal.user.notifications;

import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

public abstract class HangarInvite {

    private final long roleId;
    private final String title;
    private final String name;
    private final String url;
    private final OffsetDateTime createdAt;

    protected HangarInvite(final long roleId, final String title, final String name, final String url, final OffsetDateTime createdAt) {
        this.roleId = roleId;
        this.title = title;
        this.name = name;
        this.url = url;
        this.createdAt = createdAt;
    }

    public long getRoleId() {
        return this.roleId;
    }

    public String getTitle() {
        return this.title;
    }

    public abstract InviteType getType();

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public static class HangarProjectInvite extends HangarInvite {

        private final @Nullable String representingOrg;

        public HangarProjectInvite(final long roleId, final String title, final String name, final String url, final OffsetDateTime createdAt, final @Nullable String representingOrg) {
            super(roleId, title, name, url, createdAt);
            this.representingOrg = representingOrg;
        }

        @Override
        public InviteType getType() {
            return InviteType.PROJECT;
        }

        public @Nullable String getRepresentingOrg() {
            return this.representingOrg;
        }
    }

    public static class HangarOrganizationInvite extends HangarInvite {

        public HangarOrganizationInvite(final long roleId, final String title, final String name, final String url, final OffsetDateTime createdAt) {
            super(roleId, title, name, url, createdAt);
        }

        @Override
        public InviteType getType() {
            return InviteType.ORGANIZATION;
        }
    }

    public enum InviteType {
        PROJECT,
        ORGANIZATION;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
