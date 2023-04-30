package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.LoggedActionsDAO;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.log.LoggedActionTable;
import io.papermc.hangar.model.internal.logs.HangarLoggedAction;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.model.internal.logs.contexts.PageContext;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.util.RequestUtil;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserActionLogService extends HangarComponent {

    private final LoggedActionsDAO loggedActionsDAO;

    @Autowired
    public UserActionLogService(final LoggedActionsDAO loggedActionsDAO) {
        this.loggedActionsDAO = loggedActionsDAO;
    }

    @Transactional
    public void project(final LoggedAction<ProjectContext> action) {
        this.log(this.loggedActionsDAO::insertProjectLog, action);
    }

    @Transactional
    public void projectPage(final LoggedAction<PageContext> action) {
        this.log(this.loggedActionsDAO::insertProjectPageLog, action);
    }

    @Transactional
    public void version(final LoggedAction<VersionContext> action) {
        this.log(this.loggedActionsDAO::insertVersionLog, action);
    }

    @Transactional
    public void user(final LoggedAction<UserContext> action) {
        this.log(this.loggedActionsDAO::insertUserLog, action);
    }

    @Transactional
    public void organization(final LoggedAction<OrganizationContext> action) {
        this.log(this.loggedActionsDAO::insertOrganizationLog, action);
    }

    private <LT extends LoggedActionTable, LC extends LogContext<LT, LC>> void log(final Consumer<LT> inserter, final LoggedAction<LC> action) {
        inserter.accept(action.getContext().createTable(this.getHangarPrincipal().getUserId(), RequestUtil.getRemoteInetAddress(this.request), action));
    }

    @Transactional(readOnly = true)
    public PaginatedResult<HangarLoggedAction> getLogs(final RequestPagination pagination) {
        final List<HangarLoggedAction> log = this.loggedActionsDAO.getLog(pagination);
        if (!this.getHangarPrincipal().isAllowedGlobal(Permission.ViewIp)) {
            for (final HangarLoggedAction hangarLoggedAction : log) {
                hangarLoggedAction.hideAddress();
            }
        }
        return new PaginatedResult<>(new Pagination(this.loggedActionsDAO.getLogCount(pagination), pagination), log);
    }
}
