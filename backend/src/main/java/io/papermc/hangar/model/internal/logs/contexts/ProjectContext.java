package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionsProjectTable;

public class ProjectContext extends LogContext<LoggedActionsProjectTable, ProjectContext> {

    private final Long projectId;

    private ProjectContext(Long projectId) {
        super(Context.PROJECT, LoggedActionsProjectTable::new);
        this.projectId = projectId;
    }

    public final Long getProjectId() {
        return projectId;
    }

    public static ProjectContext of(Long projectId) {
        return new ProjectContext(projectId);
    }
}
