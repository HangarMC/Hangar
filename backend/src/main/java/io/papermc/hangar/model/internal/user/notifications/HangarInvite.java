package io.papermc.hangar.model.internal.user.notifications;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public abstract class HangarInvite {

    private final long roleId;
    private final String role;
    private final String name;
    private final String url;

    protected HangarInvite(final long roleId, final String role, final String name, final String url) {
        this.roleId = roleId;
        this.role = role;
        this.name = name;
        this.url = url;
    }

    public long getRoleId() {
        return this.roleId;
    }

    public String getRole() {
        return this.role;
    }

    public abstract InviteType getType();

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public static class HangarProjectInvite extends HangarInvite {

        private final String representingOrg;

        public HangarProjectInvite(final long roleId, final String role, final String name, final String url, final @Nullable String representingOrg) {
            super(roleId, role, name, url);
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

        public HangarOrganizationInvite(final long roleId, final String role, final String name, final String url) {
            super(roleId, role, name, url);
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
