package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.roles.ProjectRoleDAO;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite;
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InviteService extends HangarService {

    private final HangarNotificationsDAO hangarNotificationsDAO;

    @Autowired
    public InviteService(HangarDao<HangarNotificationsDAO> hangarNotificationsDAO, HangarDao<ProjectRoleDAO> projectRoleDAO) {
        this.hangarNotificationsDAO = hangarNotificationsDAO.get();
    }

    public List<HangarInvite.HangarProjectInvite> getProjectInvites() {
        return hangarNotificationsDAO.getProjectInvites(getHangarPrincipal().getId());
    }

    public List<HangarInvite.HangarOrganizationInvite> getOrganizationInvites() {
        return hangarNotificationsDAO.getOrganizationInvites(getHangarPrincipal().getId());
    }
}
