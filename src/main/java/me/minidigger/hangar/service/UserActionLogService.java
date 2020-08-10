package me.minidigger.hangar.service;

import me.minidigger.hangar.db.customtypes.LoggedActionType;
import me.minidigger.hangar.db.customtypes.LoggedActionType.OrganizationContext;
import me.minidigger.hangar.db.customtypes.LoggedActionType.ProjectContext;
import me.minidigger.hangar.db.customtypes.LoggedActionType.ProjectPageContext;
import me.minidigger.hangar.db.customtypes.LoggedActionType.UserContext;
import me.minidigger.hangar.db.customtypes.LoggedActionType.VersionContext;
import me.minidigger.hangar.db.dao.ActionsDao;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.model.LoggedActionsOrganizationTable;
import me.minidigger.hangar.db.model.LoggedActionsPageTable;
import me.minidigger.hangar.db.model.LoggedActionsProjectTable;
import me.minidigger.hangar.db.model.LoggedActionsUserTable;
import me.minidigger.hangar.db.model.LoggedActionsVersionTable;
import me.minidigger.hangar.model.viewhelpers.LoggedActionViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserActionLogService {

    private final HangarDao<ActionsDao> actionsDao;
    private final UserService userService;

    @Autowired
    public UserActionLogService(HangarDao<ActionsDao> actionsDao, UserService userService) {
        this.actionsDao = actionsDao;
        this.userService = userService;
    }

    public void project(HttpServletRequest request, LoggedActionType<ProjectContext> loggedActionType, String newState, String oldState) {
        LoggedActionsProjectTable log = new LoggedActionsProjectTable(
                userService.getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getProjectId(),
                newState,
                oldState
        );
        actionsDao.get().insertProjectLog(log);
    }

    public void projectPage(HttpServletRequest request, LoggedActionType<ProjectPageContext> loggedActionType, String newState, String oldState) {
        LoggedActionsPageTable log = new LoggedActionsPageTable(
                userService.getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getProjectId(),
                loggedActionType.getActionContext().getPageId(),
                Objects.toString(newState, ""),
                Objects.toString(oldState, "")
        );
        actionsDao.get().insertProjectPageLog(log);
    }

    public void version(HttpServletRequest request, LoggedActionType<VersionContext> loggedActionType, String newState, String oldState) {
        LoggedActionsVersionTable log = new LoggedActionsVersionTable(
                userService.getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getProjectId(),
                loggedActionType.getActionContext().getVersionId(),
                Objects.toString(newState, ""),
                Objects.toString(oldState, "")
        );
        actionsDao.get().insertVersionLog(log);
    }

    public void user(HttpServletRequest request, LoggedActionType<UserContext> loggedActionType, String newState, String oldState) {
        LoggedActionsUserTable log = new LoggedActionsUserTable(
                userService.getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getUserId(),
                Objects.toString(newState, ""),
                Objects.toString(oldState, "")
        );
        actionsDao.get().insertUserLog(log);
    }

    public void organization(HttpServletRequest request, LoggedActionType<OrganizationContext> loggedActionType, String newState, String oldState) {
        LoggedActionsOrganizationTable log = new LoggedActionsOrganizationTable(
                userService.getCurrentUser().getId(),
                getInetAddress(request),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getOrganizationLog(),
                Objects.toString(newState, ""),
                Objects.toString(oldState, "")
        );
        actionsDao.get().insertOrganizationLog(log);
    }

    public List<LoggedActionViewModel> getLog(Integer oPage, Object userFilter, Object projectFilter, Object versionFilter, Object pageFilter, Object actionFilter, Object subjectFilter) {
        return new ArrayList<>(); //TODO See AppQueries.getLog(...)
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

