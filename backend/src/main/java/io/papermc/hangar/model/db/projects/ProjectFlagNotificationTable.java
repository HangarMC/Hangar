package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectFlagNotificationTable extends Table {

    private final long flagId;
    private final long notificationId;

    @JdbiConstructor
    public ProjectFlagNotificationTable(final long id, final long flagId, final long notificationId) {
        super(id);
        this.flagId = flagId;
        this.notificationId = notificationId;
    }

    public ProjectFlagNotificationTable(final long flagId, final long notificationId) {
        this.flagId = flagId;
        this.notificationId = notificationId;
    }

    public long getFlagId() {
        return this.flagId;
    }

    public long getNotificationId() {
        return this.notificationId;
    }

    @Override
    public String toString() {
        return "ProjectFlagNotificationTable{" +
            "flagId=" + this.flagId +
            ", notificationId=" + this.notificationId +
            '}';
    }
}
