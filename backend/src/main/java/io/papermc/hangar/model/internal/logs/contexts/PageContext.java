package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionsPageTable;

public class PageContext extends LogContext<LoggedActionsPageTable, PageContext> {

    private final Long projectId;
    private final Long pageId;

    private PageContext(Long projectId, Long pageId) {
        super(Context.PAGE, LoggedActionsPageTable::new);
        this.projectId = projectId;
        this.pageId = pageId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getPageId() {
        return pageId;
    }

    public static PageContext of(Long projectId, Long pageId) {
        return new PageContext(projectId, pageId);
    }
}
