package io.papermc.hangar.model.internal.user;

import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.roles.GlobalRole;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HangarUser that = (HangarUser) o;
        return id == that.id && Objects.equals(headerData, that.headerData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, headerData);
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HeaderData that = (HeaderData) o;
            return hasNotice == that.hasNotice && hasUnreadNotifications == that.hasUnreadNotifications && unresolvedFlags == that.unresolvedFlags && hasProjectApprovals == that.hasProjectApprovals && hasReviewQueue == that.hasReviewQueue && globalPermission.equals(that.globalPermission);
        }

        @Override
        public int hashCode() {
            return Objects.hash(globalPermission, hasNotice, hasUnreadNotifications, unresolvedFlags, hasProjectApprovals, hasReviewQueue);
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
