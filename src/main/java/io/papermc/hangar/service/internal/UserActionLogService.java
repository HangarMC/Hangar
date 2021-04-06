package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.LoggedActionsDAO;
import io.papermc.hangar.model.db.log.LoggedActionTable;
import io.papermc.hangar.model.internal.logs.HangarLoggedAction;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.model.internal.logs.contexts.PageContext;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
public class UserActionLogService extends HangarService {

    private final LoggedActionsDAO loggedActionsDAO;

    @Autowired
    public UserActionLogService(HangarDao<LoggedActionsDAO> loggedActionsDAO) {
        this.loggedActionsDAO = loggedActionsDAO.get();
    }

    public void project(LoggedAction<ProjectContext> action) {
        log(loggedActionsDAO::insertProjectLog, action);
    }

    public void projectPage(LoggedAction<PageContext> action) {
        log(loggedActionsDAO::insertProjectPageLog, action);
    }

    public void version(LoggedAction<VersionContext> action) {
        log(loggedActionsDAO::insertVersionLog, action);
    }

    public void user(LoggedAction<UserContext> action) {
        log(loggedActionsDAO::insertUserLog, action);
    }

    public void organization(LoggedAction<OrganizationContext> action) {
        log(loggedActionsDAO::insertOrganizationLog, action);
    }

    @Transactional
    public  <LT extends LoggedActionTable, LC extends LogContext<LT, LC>> void log(Consumer<LT> inserter, LoggedAction<LC> action) {
        inserter.accept(action.getContext().createTable(getHangarPrincipal().getUserId(), RequestUtil.getRemoteInetAddress(request), action));
    }

    public List<HangarLoggedAction> getLog(Integer oPage, String userFilter, String projectFilter, String versionFilter, String pageFilter, String actionFilter, String subjectFilter) {
        long pageSize = 50L;
        long offset;
        if (oPage == null) {
            offset = 0;
        } else {
            offset = oPage * pageSize;
        }
        return loggedActionsDAO.getLog(userFilter, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter, offset, pageSize);
    }
}
