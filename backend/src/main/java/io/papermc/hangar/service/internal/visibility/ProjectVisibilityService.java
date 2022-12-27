package io.papermc.hangar.service.internal.visibility;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.VisibilityDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.visibility.ProjectVisibilityChangeTable;
import io.papermc.hangar.model.internal.job.UpdateDiscourseProjectTopicJob;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.service.internal.JobService;

import java.util.Map;

@Service
public class ProjectVisibilityService extends VisibilityService<ProjectContext, ProjectTable, ProjectVisibilityChangeTable> {

    private final ProjectsDAO projectsDAO;
    private final VisibilityDAO visibilityDAO;
    private final JobService jobService;
    private final HangarProjectsDAO hangarProjectsDAO;

    @Autowired
    public ProjectVisibilityService(final VisibilityDAO visibilityDAO, final ProjectsDAO projectsDAO, final JobService jobService, final HangarProjectsDAO hangarProjectsDAO) {
        super(ProjectVisibilityChangeTable::new, LogAction.PROJECT_VISIBILITY_CHANGED);
        this.projectsDAO = projectsDAO;
        this.visibilityDAO = visibilityDAO;
        this.jobService = jobService;
        this.hangarProjectsDAO = hangarProjectsDAO;
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
    protected void postUpdate(final @Nullable ProjectTable model) {
        if (model != null) {
            this.jobService.save(new UpdateDiscourseProjectTopicJob(model.getId()));
        }
        this.hangarProjectsDAO.refreshHomeProjects();
    }

    @Override
    public Map.Entry<String, ProjectVisibilityChangeTable> getLastVisibilityChange(final long principalId) {
        return this.visibilityDAO.getLatestProjectVisibilityChange(principalId);
    }
}
