package io.papermc.hangar.service.internal.visibility;

import io.papermc.hangar.db.dao.internal.table.VisibilityDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.visibility.ProjectVisibilityChangeTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectVisibilityService extends VisibilityService<ProjectContext, ProjectTable, ProjectVisibilityChangeTable> {

    private final ProjectsDAO projectsDAO;
    private final VisibilityDAO visibilityDAO;

    @Autowired
    public ProjectVisibilityService(final VisibilityDAO visibilityDAO, final ProjectsDAO projectsDAO) {
        super(ProjectVisibilityChangeTable::new, LogAction.PROJECT_VISIBILITY_CHANGED);
        this.projectsDAO = projectsDAO;
        this.visibilityDAO = visibilityDAO;
    }

    @Override
    void updateLastVisChange(final long currentUserId, final long modelId) {
        this.visibilityDAO.updateLatestProjectChange(currentUserId, modelId);
    }

    @Override
    public ProjectTable getModel(final long id) {
        return this.projectsDAO.getById(id);
    }

    @Override
    ProjectTable updateModel(final ProjectTable model) {
        return this.projectsDAO.update(model);
    }

    @Override
    void insertNewVisibilityEntry(final ProjectVisibilityChangeTable visibilityTable) {
        this.visibilityDAO.insert(visibilityTable);
    }

    @Override
    public Map.Entry<String, ProjectVisibilityChangeTable> getLastVisibilityChange(final long principalId) {
        return this.visibilityDAO.getLatestProjectVisibilityChange(principalId);
    }
}
