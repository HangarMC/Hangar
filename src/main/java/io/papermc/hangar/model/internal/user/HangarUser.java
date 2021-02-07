package io.papermc.hangar.model.internal.user;

import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.roles.GlobalRole;

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
        private final long unreadNotifications;
        private final long unresolvedFlags;
        private final long projectApprovals;
        private final long reviewQueueCount;

        public HeaderData(Permission globalPermission, long unreadNotifications, long unresolvedFlags, long projectApprovals, long reviewQueueCount) {
            this.globalPermission = globalPermission;
            this.unreadNotifications = unreadNotifications;
            this.unresolvedFlags = unresolvedFlags;
            this.projectApprovals = projectApprovals;
            this.reviewQueueCount = reviewQueueCount;
        }

        public Permission getGlobalPermission() {
            return globalPermission;
        }

        public long getUnreadNotifications() {
            return unreadNotifications;
        }

        public long getUnresolvedFlags() {
            return unresolvedFlags;
        }

        public long getProjectApprovals() {
            return projectApprovals;
        }

        public long getReviewQueueCount() {
            return reviewQueueCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HeaderData that = (HeaderData) o;
            return unreadNotifications == that.unreadNotifications && unresolvedFlags == that.unresolvedFlags && projectApprovals == that.projectApprovals && reviewQueueCount == that.reviewQueueCount && globalPermission.equals(that.globalPermission);
        }

        @Override
        public int hashCode() {
            return Objects.hash(globalPermission, unreadNotifications, unresolvedFlags, projectApprovals, reviewQueueCount);
        }

        @Override
        public String toString() {
            return "HeaderData{" +
                    "globalPermission=" + globalPermission +
                    ", unreadNotifications=" + unreadNotifications +
                    ", unresolvedFlags=" + unresolvedFlags +
                    ", projectApprovals=" + projectApprovals +
                    ", reviewQueueCount=" + reviewQueueCount +
                    '}';
        }
    }
}
