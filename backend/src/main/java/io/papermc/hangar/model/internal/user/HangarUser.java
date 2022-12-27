package io.papermc.hangar.model.internal.user;

import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.UserNameChange;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.roles.GlobalRole;

import java.time.OffsetDateTime;
import java.util.List;
import javax.annotation.Nullable;

public class HangarUser extends User implements Identified {

    private final long id;
    private HeaderData headerData;
    private final List<Integer> readPrompts;
    private final String language;
    private final String theme;
    private String accessToken;

    public HangarUser(final OffsetDateTime createdAt, final String name, final String tagline, final OffsetDateTime joinDate, final List<GlobalRole> roles, final long projectCount, final boolean locked, @Nullable final List<UserNameChange> nameHistory, final long id, final List<Integer> readPrompts, final String language, final String theme) {
        super(createdAt, name, tagline, joinDate, roles, projectCount, locked, nameHistory);
        this.id = id;
        this.readPrompts = readPrompts;
        this.language = language;
        this.theme = theme;
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

    public User toUser() {
        return new User(
                this.getCreatedAt(),
                this.getName(),
                this.getTagline(),
                this.getJoinDate(),
                this.getRoles(),
                this.getProjectCount(),
                this.isLocked(),
                this.getNameHistory()
            );
    }

    public static class HeaderData {
        private final Permission globalPermission;
        private final long unreadNotifications;
        private final long unansweredInvites;
        private final long unresolvedFlags;
        private final long projectApprovals;
        private final long reviewQueueCount;
        private final long organizationCount;

        public HeaderData(final Permission globalPermission, final long unreadNotifications, final long unansweredInvites, final long unresolvedFlags, final long projectApprovals, final long reviewQueueCount, final long organizationCount) {
            this.globalPermission = globalPermission;
            this.unreadNotifications = unreadNotifications;
            this.unansweredInvites = unansweredInvites;
            this.unresolvedFlags = unresolvedFlags;
            this.projectApprovals = projectApprovals;
            this.reviewQueueCount = reviewQueueCount;
            this.organizationCount = organizationCount;
        }

        public Permission getGlobalPermission() {
            return this.globalPermission;
        }

        public long getUnreadNotifications() {
            return this.unreadNotifications;
        }

        public long getUnansweredInvites() {
            return this.unansweredInvites;
        }

        public long getUnresolvedFlags() {
            return this.unresolvedFlags;
        }

        public long getProjectApprovals() {
            return this.projectApprovals;
        }

        public long getReviewQueueCount() {
            return this.reviewQueueCount;
        }

        public long getOrganizationCount() {
            return this.organizationCount;
        }

        @Override
        public String toString() {
            return "HeaderData{" +
                    "globalPermission=" + this.globalPermission +
                    ", unreadNotifications=" + this.unreadNotifications +
                    ", unansweredInvites=" + this.unansweredInvites +
                    ", unresolvedFlags=" + this.unresolvedFlags +
                    ", projectApprovals=" + this.projectApprovals +
                    ", reviewQueueCount=" + this.reviewQueueCount +
                    ", organizationCount=" + this.organizationCount +
                    '}';
        }
    }
}
