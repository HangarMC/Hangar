package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.LoggedActionsDAO;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.requests.RequestPagination;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
public class UserActionLogService extends HangarComponent {

    private final LoggedActionsDAO loggedActionsDAO;

    @Autowired
    public UserActionLogService(LoggedActionsDAO loggedActionsDAO) {
        this.loggedActionsDAO = loggedActionsDAO;
    }

    @Transactional
    public void project(LoggedAction<ProjectContext> action) {
        log(loggedActionsDAO::insertProjectLog, action);
    }

    @Transactional
    public void projectPage(LoggedAction<PageContext> action) {
        log(loggedActionsDAO::insertProjectPageLog, action);
    }

    @Transactional
    public void version(LoggedAction<VersionContext> action) {
        log(loggedActionsDAO::insertVersionLog, action);
    }

    @Transactional
    public void user(LoggedAction<UserContext> action) {
        log(loggedActionsDAO::insertUserLog, action);
    }

    @Transactional
    public void organization(LoggedAction<OrganizationContext> action) {
        log(loggedActionsDAO::insertOrganizationLog, action);
    }

    private <LT extends LoggedActionTable, LC extends LogContext<LT, LC>> void log(Consumer<LT> inserter, LoggedAction<LC> action) {
        inserter.accept(action.getContext().createTable(getHangarPrincipal().getUserId(), RequestUtil.getRemoteInetAddress(request), action));
    }

    @Transactional
    public PaginatedResult<HangarLoggedAction> getLogs(RequestPagination pagination) {
        return new PaginatedResult<>(new Pagination(loggedActionsDAO.getLogCount(pagination), pagination), loggedActionsDAO.getLog(pagination));
    }
}
