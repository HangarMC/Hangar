package io.papermc.hangar.service.internal.visibility;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.VisibilityDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.visibility.ProjectVisibilityChangeTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map.Entry;

@Service
public class ProjectVisibilityService extends VisibilityService<ProjectTable, ProjectVisibilityChangeTable> {

    private final ProjectsDAO projectsDAO;
    private final VisibilityDAO visibilityDAO;
    private final HangarProjectsDAO hangarProjectsDAO;

    @Autowired
    public ProjectVisibilityService(HangarDao<VisibilityDAO> visibilityDAO, HangarDao<ProjectsDAO> projectsDAO, HangarDao<HangarProjectsDAO> hangarProjectsDAO) {
        super(ProjectVisibilityChangeTable::new);
        this.projectsDAO = projectsDAO.get();
        this.visibilityDAO = visibilityDAO.get();
        this.hangarProjectsDAO = hangarProjectsDAO.get();
    }

    @Override
    public ProjectTable changeVisibility(ProjectTable model, Visibility newVisibility, String comment) {
        if (model == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        Visibility oldVis = model.getVisibility();
        // TODO add logging for visibility for versions and move this to the abstract class
        userActionLogService.project(LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(model.getId())), newVisibility.getName(), oldVis.getName());
        return super.changeVisibility(model, newVisibility, comment);
    }

    @Override
    void updateLastVisChange(long currentUserId, long modelId) {
        visibilityDAO.updateLatestProjectChange(currentUserId, modelId);
    }

    @Override
    ProjectTable getModel(long id) {
        return projectsDAO.getById(id);
    }

    @Override
    ProjectTable updateModel(ProjectTable model) {
        return projectsDAO.update(model);
    }

    @Override
    void insertNewVisibilityEntry(ProjectVisibilityChangeTable visibilityTable) {
        visibilityDAO.insert(visibilityTable);
    }

    @Override
    protected void postUpdate() {
        hangarProjectsDAO.refreshHomeProjects();
    }

    @Override
    public Entry<String, ProjectVisibilityChangeTable> getLastVisibilityChange(long principalId) {
        return visibilityDAO.getLatestProjectVisibilityChange(principalId);
    }
}
