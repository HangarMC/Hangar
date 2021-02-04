package io.papermc.hangar.service.internal;

import io.papermc.hangar.controller.internal.HangarUsersController;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.roles.ProjectRoleDAO;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite;
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InviteService extends HangarService {

    private final HangarNotificationsDAO hangarNotificationsDAO;
    private final ProjectRoleDAO projectRoleDAO;

    @Autowired
    public InviteService(HangarDao<HangarNotificationsDAO> hangarNotificationsDAO, HangarDao<ProjectRoleDAO> projectRoleDAO) {
        this.hangarNotificationsDAO = hangarNotificationsDAO.get();
        this.projectRoleDAO = projectRoleDAO.get();
    }

    public List<HangarInvite.HangarProjectInvite> getProjectInvites() {
        return hangarNotificationsDAO.getProjectInvites(getHangarPrincipal().getId());
    }

    public List<HangarInvite.HangarOrganizationInvite> getOrganizationInvites() {
        return hangarNotificationsDAO.getOrganizationInvites(getHangarPrincipal().getId());
    }

    public void setProjectInviteStatus(long roleTableId, HangarUsersController.InviteStatus inviteStatus) {
        ProjectRoleTable projectRoleTable = projectRoleDAO.getTable(roleTableId);
    }

    private void updateRole(ProjectRoleTable projectRoleTable, HangarUsersController.InviteStatus inviteStatus) {
        switch (inviteStatus) {
            case ACCEPT:

        }
    }
}
