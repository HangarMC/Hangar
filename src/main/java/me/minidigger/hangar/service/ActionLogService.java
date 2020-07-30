package me.minidigger.hangar.service;

import me.minidigger.hangar.db.customtypes.LoggedActionType;
import me.minidigger.hangar.db.customtypes.LoggedActionType.ProjectContext;
import me.minidigger.hangar.db.dao.ActionsDao;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.model.LoggedActionsProjectTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class ActionLogService {

    private final HangarDao<ActionsDao> actionsDao;
    private final UserService userService;

    @Autowired
    public ActionLogService(HangarDao<ActionsDao> actionsDao, UserService userService) {
        this.actionsDao = actionsDao;
        this.userService = userService;
    }

    public void project(String remoteAddress, LoggedActionType<ProjectContext> loggedActionType, String newState, String oldState) throws UnknownHostException {
        LoggedActionsProjectTable log = new LoggedActionsProjectTable(
                userService.getCurrentUser().getId(),
                InetAddress.getByName(remoteAddress),
                loggedActionType.getValue(),
                loggedActionType.getActionContext().getProjectId(),
                newState,
                oldState
        );
        actionsDao.get().insertProjectLog(log);
    }

    public void
}

