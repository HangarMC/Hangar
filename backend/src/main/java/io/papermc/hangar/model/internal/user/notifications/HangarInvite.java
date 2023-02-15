package io.papermc.hangar.model.internal.user.notifications;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public abstract class HangarInvite {

    private final long roleId;
    private final String name;
    private final String url;

    protected HangarInvite(final long roleId, final String name, final String url) {
        this.roleId = roleId;
        this.name = name;
        this.url = url;
    }

    public long getRoleId() {
        return this.roleId;
    }

    public abstract InviteType getType();

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public static class HangarProjectInvite extends HangarInvite {

        public HangarProjectInvite(final long roleId, final String name, final String url) {
            super(roleId, name, url);
        }

        @Override
        public InviteType getType() {
            return InviteType.PROJECT;
        }
    }

    public static class HangarOrganizationInvite extends HangarInvite {

        public HangarOrganizationInvite(final long roleId, final String name, final String url) {
            super(roleId, name, url);
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
