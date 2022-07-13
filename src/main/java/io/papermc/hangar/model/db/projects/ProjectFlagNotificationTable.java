package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectFlagNotificationTable extends Table {

    private final long flagId;
    private final long notificationId;
    private final long userId;

    @JdbiConstructor
    public ProjectFlagNotificationTable(final long id, final long flagId, final long notificationId, final long userId) {
        super(id);
        this.flagId = flagId;
        this.notificationId = notificationId;
        this.userId = userId;
    }

    public ProjectFlagNotificationTable(final long flagId, final long notificationId, final long userId) {
        this.flagId = flagId;
        this.notificationId = notificationId;
        this.userId = userId;
    }

    public long getFlagId() {
        return flagId;
    }

    public long getNotificationId() {
        return notificationId;
    }

    /**
     * Returns the user id of the staff initiating the notification.
     *
     * @return user id of the staff initiating the notification
     */
    public long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "ProjectFlagNotificationTable{" +
            "flagId=" + flagId +
            ", notificationId=" + notificationId +
            '}';
    }
}
