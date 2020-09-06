package io.papermc.hangar.service;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.dao.ActionsDao;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.model.LoggedActionsOrganizationTable;
import io.papermc.hangar.db.model.LoggedActionsPageTable;
import io.papermc.hangar.db.model.LoggedActionsProjectTable;
import io.papermc.hangar.db.model.LoggedActionsUserTable;
import io.papermc.hangar.db.model.LoggedActionsVersionTable;
import io.papermc.hangar.model.viewhelpers.LoggedActionViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

@Service
public class UserActionLogService extends HangarService {

    private final HangarDao<ActionsDao> actionsDao;
    private final UserService userService;

    @Autowired
    public UserActionLogService(HangarDao<ActionsDao> actionsDao, UserService userService) {
        this.actionsDao = actionsDao;
        this.userService = userService;
    }

    public void project(HttpServletRequest request, LoggedActionType<LoggedActionType.ProjectContext> loggedActionType, String newState, String oldState) {
        LoggedActionsProjectTable log = new LoggedActionsProjectTable(
                getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getProjectId(),
                newState,
                oldState
        );
        actionsDao.get().insertProjectLog(log);
    }

    public void projectPage(HttpServletRequest request, LoggedActionType<LoggedActionType.ProjectPageContext> loggedActionType, String newState, String oldState) {
        LoggedActionsPageTable log = new LoggedActionsPageTable(
                getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getProjectId(),
                loggedActionType.getActionContext().getPageId(),
                Objects.toString(newState, ""),
                Objects.toString(oldState, "")
        );
        actionsDao.get().insertProjectPageLog(log);
    }

    public void version(HttpServletRequest request, LoggedActionType<LoggedActionType.VersionContext> loggedActionType, String newState, String oldState) {
        LoggedActionsVersionTable log = new LoggedActionsVersionTable(
                getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getProjectId(),
                loggedActionType.getActionContext().getVersionId(),
                Objects.toString(newState, ""),
                Objects.toString(oldState, "")
        );
        actionsDao.get().insertVersionLog(log);
    }

    public void user(HttpServletRequest request, LoggedActionType<LoggedActionType.UserContext> loggedActionType, String newState, String oldState) {
        LoggedActionsUserTable log = new LoggedActionsUserTable(
                getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getUserId(),
                Objects.toString(newState, ""),
                Objects.toString(oldState, "")
        );
        actionsDao.get().insertUserLog(log);
    }

    public void organization(HttpServletRequest request, LoggedActionType<LoggedActionType.OrganizationContext> loggedActionType, String newState, String oldState) {
        LoggedActionsOrganizationTable log = new LoggedActionsOrganizationTable(
                getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getOrganizationLog(),
                Objects.toString(newState, ""),
                Objects.toString(oldState, "")
        );
        actionsDao.get().insertOrganizationLog(log);
    }

    public List<LoggedActionViewModel<?>> getLog(Integer oPage, String userFilter, String projectFilter, String versionFilter, String pageFilter, String actionFilter, String subjectFilter) {
        long pageSize = 50L;
        long offset;
        if (oPage == null) {
            offset = 0;
        } else {
            offset = oPage * pageSize;
        }
        return actionsDao.get().getLog(userFilter, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter, offset, pageSize);
    }

    private InetAddress getInetAddress(HttpServletRequest request) {
        String host = request.getHeader("X-Forwarded-For");
        if (host == null) host = request.getRemoteHost();
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return null;
        }
    }
}

