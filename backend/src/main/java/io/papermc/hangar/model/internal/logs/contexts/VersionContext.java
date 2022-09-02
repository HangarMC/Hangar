package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionsVersionTable;

public class VersionContext extends LogContext<LoggedActionsVersionTable, VersionContext> {

    private final Long projectId;
    private final Long versionId;

    private VersionContext(Long projectId, Long versionId) {
        super(Context.VERSION, LoggedActionsVersionTable::new);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public static VersionContext of(Long projectId, Long versionId) {
        return new VersionContext(projectId, versionId);
    }
}
