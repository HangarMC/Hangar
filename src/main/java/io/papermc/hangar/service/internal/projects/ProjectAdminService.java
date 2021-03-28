package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsAdminDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.projects.HangarProjectApproval;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectAdminService extends HangarService {

    private final ProjectsDAO projectsDAO;
    private final HangarProjectsAdminDAO hangarProjectsAdminDAO;
    private final ProjectVisibilityService projectVisibilityService;

    @Autowired
    public ProjectAdminService(HangarDao<ProjectsDAO> projectsDAO, HangarDao<HangarProjectsAdminDAO> hangarProjectsAdminDAO, ProjectVisibilityService projectVisibilityService) {
        this.projectsDAO = projectsDAO.get();
        this.hangarProjectsAdminDAO = hangarProjectsAdminDAO.get();
        this.projectVisibilityService = projectVisibilityService;
    }

    public void sendProjectForApproval(long projectId) {
        ProjectTable projectTable = projectsDAO.getById(projectId);
        if (projectTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (projectTable.getVisibility() != Visibility.NEEDSCHANGES) {
            throw new HangarApiException();
        }
        projectVisibilityService.changeVisibility(projectTable, Visibility.NEEDSAPPROVAL, "");
    }

    public List<HangarProjectApproval> getProjectsNeedingApproval() {
        return hangarProjectsAdminDAO.getVisibilityNeedsApproval();
    }

    public List<HangarProjectApproval> getProjectsWaitingForChanges() {
        return hangarProjectsAdminDAO.getVisibilityWaitingProject();
    }
}
