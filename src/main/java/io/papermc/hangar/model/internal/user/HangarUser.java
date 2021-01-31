package io.papermc.hangar.model.internal.user;

import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.roles.GlobalRole;

import java.time.OffsetDateTime;
import java.util.List;

public class HangarUser extends User implements Identified {

    private final long id;
    private HeaderData headerData;

    public HangarUser(OffsetDateTime createdAt, String name, String tagline, OffsetDateTime joinDate, List<GlobalRole> roles, long projectCount, long id) {
        super(createdAt, name, tagline, joinDate, roles, projectCount);
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public HeaderData getHeaderData() {
        return headerData;
    }

    public void setHeaderData(HeaderData headerData) {
        this.headerData = headerData;
    }

    public User toUser() {
        return new User(
                this.getCreatedAt(),
                this.getName(),
                this.getTagline(),
                this.getJoinDate(),
                this.getRoles(),
                this.getProjectCount());
    }

    @Override
    public String toString() {
        return "HangarUser{" +
                "id=" + id +
                ", headerData=" + headerData +
                "} " + super.toString();
    }

    public static class HeaderData {
        private final Permission globalPermission;
        private final boolean hasNotice;
        private final boolean hasUnreadNotifications;
        private final boolean unresolvedFlags;
        private final boolean hasProjectApprovals;
        private final boolean hasReviewQueue;

        public HeaderData(Permission globalPermission, boolean hasNotice, boolean hasUnreadNotifications, boolean unresolvedFlags, boolean hasProjectApprovals, boolean hasReviewQueue) {
            this.globalPermission = globalPermission;
            this.hasNotice = hasNotice;
            this.hasUnreadNotifications = hasUnreadNotifications;
            this.unresolvedFlags = unresolvedFlags;
            this.hasProjectApprovals = hasProjectApprovals;
            this.hasReviewQueue = hasReviewQueue;
        }

        public Permission getGlobalPermission() {
            return globalPermission;
        }

        public boolean isHasNotice() {
            return hasNotice;
        }

        public boolean isHasUnreadNotifications() {
            return hasUnreadNotifications;
        }

        public boolean isUnresolvedFlags() {
            return unresolvedFlags;
        }

        public boolean isHasProjectApprovals() {
            return hasProjectApprovals;
        }

        public boolean isHasReviewQueue() {
            return hasReviewQueue;
        }

        @Override
        public String toString() {
            return "HeaderData{" +
                    "globalPermission=" + globalPermission +
                    ", hasNotice=" + hasNotice +
                    ", hasUnreadNotifications=" + hasUnreadNotifications +
                    ", unresolvedFlags=" + unresolvedFlags +
                    ", hasProjectApprovals=" + hasProjectApprovals +
                    ", hasReviewQueue=" + hasReviewQueue +
                    '}';
        }
    }
}
