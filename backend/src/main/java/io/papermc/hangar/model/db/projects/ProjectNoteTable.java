package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectNoteTable extends Table {

    private final long projectId;
    private final String message;
    private final Long userId;

    @JdbiConstructor
    public ProjectNoteTable(final OffsetDateTime createdAt, final long id, final long projectId, final String message, final Long userId) {
        super(createdAt, id);
        this.projectId = projectId;
        this.message = message;
        this.userId = userId;
    }

    public ProjectNoteTable(final long projectId, final String message, final Long userId) {
        this.projectId = projectId;
        this.message = message;
        this.userId = userId;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public String getMessage() {
        return this.message;
    }

    public Long getUserId() {
        return this.userId;
    }

    @Override
    public String toString() {
        return "ProjectNoteTable{" +
            "projectId=" + this.projectId +
            ", message='" + this.message + '\'' +
            ", userId=" + this.userId +
            "} " + super.toString();
    }
}
