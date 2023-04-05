package io.papermc.hangar.model.internal.user;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.UserNameChange;
import io.papermc.hangar.model.common.Permission;
import jakarta.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class HangarUser extends User implements Identified {

    private final long id;
    private final UUID uuid;
    private final String email;
    private HeaderData headerData;
    private final List<Integer> readPrompts;
    private final String language;
    private final String theme;
    private String accessToken;
    private final JSONB socials;

    public HangarUser(final OffsetDateTime createdAt, final String name, final String tagline, final List<Long> roles, final long projectCount, final boolean locked, @Nullable final List<UserNameChange> nameHistory, final long id, final UUID uuid, final String email, final List<Integer> readPrompts, final String language, final String theme, final JSONB socials) {
        super(createdAt, name, tagline, roles, projectCount, locked, nameHistory);
        this.id = id;
        this.uuid = uuid;
        this.email = email;
        this.readPrompts = readPrompts;
        this.language = language;
        this.theme = theme;
        this.socials = socials;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public HeaderData getHeaderData() {
        return this.headerData;
    }

    public void setHeaderData(final HeaderData headerData) {
        this.headerData = headerData;
    }

    public List<Integer> getReadPrompts() {
        return this.readPrompts;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getTheme() {
        return this.theme;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getEmail() {
        return this.email;
    }

    public JSONB getSocials() {
        return this.socials;
    }

    public record HeaderData(Permission globalPermission,
                             long unreadNotifications,
                             long unansweredInvites,
                             long unresolvedFlags,
                             long projectApprovals,
                             long reviewQueueCount,
                             long organizationCount) {
    }
}
