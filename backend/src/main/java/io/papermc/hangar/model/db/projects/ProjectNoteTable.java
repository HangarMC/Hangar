package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectNoteTable extends Table {

    private final long projectId;
    private final String message;
    private final Long userId;

    @JdbiConstructor
    public ProjectNoteTable(OffsetDateTime createdAt, long id, long projectId, String message, Long userId) {
        super(createdAt, id);
        this.projectId = projectId;
        this.message = message;
        this.userId = userId;
    }

    public ProjectNoteTable(long projectId, String message, Long userId) {
        this.projectId = projectId;
        this.message = message;
        this.userId = userId;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "ProjectNoteTable{" +
                "projectId=" + projectId +
                ", message='" + message + '\'' +
                ", userId=" + userId +
                "} " + super.toString();
    }
}
