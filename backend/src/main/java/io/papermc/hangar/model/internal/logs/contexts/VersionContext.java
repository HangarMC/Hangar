package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionsVersionTable;

public class VersionContext extends LogContext<LoggedActionsVersionTable, VersionContext> {

    private final Long projectId;
    private final Long versionId;

    private VersionContext(final Long projectId, final Long versionId) {
        super(Context.VERSION, LoggedActionsVersionTable::new);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    public Long getProjectId() {
        return this.projectId;
    }

    public Long getVersionId() {
        return this.versionId;
    }

    public static VersionContext of(final Long projectId, final Long versionId) {
        return new VersionContext(projectId, versionId);
    }
}
