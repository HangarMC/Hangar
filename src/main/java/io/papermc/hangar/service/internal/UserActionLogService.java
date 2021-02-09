package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.OrganizationContext;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectPageContext;
import io.papermc.hangar.db.customtypes.LoggedActionType.UserContext;
import io.papermc.hangar.db.customtypes.LoggedActionType.VersionContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.LoggedActionsDAO;
import io.papermc.hangar.model.db.log.LoggedActionsOrganizationTable;
import io.papermc.hangar.model.db.log.LoggedActionsPageTable;
import io.papermc.hangar.model.db.log.LoggedActionsProjectTable;
import io.papermc.hangar.model.db.log.LoggedActionsUserTable;
import io.papermc.hangar.model.db.log.LoggedActionsVersionTable;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserActionLogService extends HangarService {

    private final LoggedActionsDAO loggedActionsDAO;

    @Autowired
    public UserActionLogService(HangarDao<LoggedActionsDAO> loggedActionsDAO) {
        this.loggedActionsDAO = loggedActionsDAO.get();
    }

    public void project(LoggedActionType<ProjectContext> loggedActionType, String newState, String oldState) {
        loggedActionsDAO.insertProjectLog(new LoggedActionsProjectTable(
                getHangarPrincipal().getUserId(),
                RequestUtil.getRemoteInetAddress(request),
                loggedActionType.getValue(),
                newState,
                oldState,
                loggedActionType.getActionContext().getProjectId()
        ));
    }

    public void projectPage(LoggedActionType<ProjectPageContext> loggedActionType, String newState, String oldState) {
        loggedActionsDAO.insertProjectPageLog(new LoggedActionsPageTable(
                getHangarPrincipal().getUserId(),
                RequestUtil.getRemoteInetAddress(request),
                loggedActionType.getValue(),
                newState,
                oldState,
                loggedActionType.getActionContext().getProjectId(),
                loggedActionType.getActionContext().getPageId()
        ));
    }

    public void version(LoggedActionType<VersionContext> loggedActionType, String newState, String oldState) {
        loggedActionsDAO.insertVersionLog(new LoggedActionsVersionTable(
                getHangarPrincipal().getUserId(),
                RequestUtil.getRemoteInetAddress(request),
                loggedActionType.getValue(),
                newState,
                oldState,
                loggedActionType.getActionContext().getProjectId(),
                loggedActionType.getActionContext().getVersionId()
        ));
    }

    public void user(LoggedActionType<UserContext> loggedActionType, String newState, String oldState) {
        loggedActionsDAO.insertUserLog(new LoggedActionsUserTable(
                getHangarPrincipal().getUserId(),
                RequestUtil.getRemoteInetAddress(request),
                loggedActionType.getValue(),
                newState,
                oldState,
                loggedActionType.getActionContext().getUserId()
        ));
    }

    public void organization(LoggedActionType<OrganizationContext> loggedActionType, String newState, String oldState) {
        loggedActionsDAO.insertOrganizationLog(new LoggedActionsOrganizationTable(
                getHangarPrincipal().getUserId(),
                RequestUtil.getRemoteInetAddress(request),
                loggedActionType.getValue(),
                newState,
                oldState,
                loggedActionType.getActionContext().getOrganizationId()
        ));
    }
}
