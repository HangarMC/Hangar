package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionsProjectTable;

public class ProjectContext extends LogContext<LoggedActionsProjectTable, ProjectContext> {

    private final Long projectId;

    private ProjectContext(final Long projectId) {
        super(Context.PROJECT, LoggedActionsProjectTable::new);
        this.projectId = projectId;
    }

    public final Long getProjectId() {
        return this.projectId;
    }

    public static ProjectContext of(final Long projectId) {
        return new ProjectContext(projectId);
    }
}
