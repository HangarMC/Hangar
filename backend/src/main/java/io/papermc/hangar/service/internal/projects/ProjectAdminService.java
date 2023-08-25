package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsAdminDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.projects.HangarProjectApproval;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import io.papermc.hangar.util.StringUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectAdminService extends HangarComponent {

    private final ProjectsDAO projectsDAO;
    private final HangarProjectsAdminDAO hangarProjectsAdminDAO;
    private final ProjectVisibilityService projectVisibilityService;
    private final ProjectFactory projectFactory;
    private final NotificationService notificationService;

    @Autowired
    public ProjectAdminService(final ProjectsDAO projectsDAO, final HangarProjectsAdminDAO hangarProjectsAdminDAO, final ProjectVisibilityService projectVisibilityService, final ProjectFactory projectFactory, final NotificationService notificationService) {
        this.projectsDAO = projectsDAO;
        this.hangarProjectsAdminDAO = hangarProjectsAdminDAO;
        this.projectVisibilityService = projectVisibilityService;
        this.projectFactory = projectFactory;
        this.notificationService = notificationService;
    }

    public void sendProjectForApproval(final long projectId) {
        final ProjectTable projectTable = this.projectsDAO.getById(projectId);
        if (projectTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (projectTable.getVisibility() != Visibility.NEEDSCHANGES) {
            throw new HangarApiException();
        }
        this.projectVisibilityService.changeVisibility(projectTable, Visibility.NEEDSAPPROVAL, "");
    }

    @Transactional
    public void changeVisibility(final long projectId, final Visibility visibility, final String comment) {
        final ProjectTable projectTable = this.projectVisibilityService.getModel(projectId);
        final Visibility oldVisibility = projectTable.getVisibility();
        if (oldVisibility == Visibility.SOFTDELETE && visibility != Visibility.SOFTDELETE) {
            final int suffixIndex = projectTable.getName().indexOf(ProjectFactory.SOFT_DELETION_SUFFIX);
            if (suffixIndex != -1) {
                final String newName = projectTable.getName().substring(0, suffixIndex);
                this.projectFactory.renameProject(projectTable.getName(), newName, true);
                projectTable.setName(newName);
                projectTable.setSlug(StringUtils.slugify(newName));
            }
            this.projectVisibilityService.changeVisibility(projectId, visibility, comment);
        } else if (visibility == Visibility.SOFTDELETE) {
            this.projectFactory.softDelete(projectTable, comment);
        } else {
            this.projectVisibilityService.changeVisibility(projectId, visibility, comment);
        }

        if (oldVisibility != visibility) {
            this.notificationService.notifyVisibilityChange(projectTable, visibility, comment);
        }
    }

    public List<HangarProjectApproval> getProjectsNeedingApproval() {
        return this.hangarProjectsAdminDAO.getVisibilityNeedsApproval();
    }

    public int getApprovalQueueSize() {
        return this.hangarProjectsAdminDAO.getProjectsNeedingApprovalSize();
    }

    public List<HangarProjectApproval> getProjectsWaitingForChanges() {
        return this.hangarProjectsAdminDAO.getVisibilityWaitingProject();
    }
}
