package io.papermc.hangar.service.internal.visibility;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.VisibilityDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.visibility.ProjectVisibilityChangeTable;
import io.papermc.hangar.model.internal.job.UpdateDiscourseProjectTopicJob;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.service.internal.JobService;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map.Entry;

@Service
public class ProjectVisibilityService extends VisibilityService<ProjectTable, ProjectVisibilityChangeTable> {

    private final ProjectsDAO projectsDAO;
    private final VisibilityDAO visibilityDAO;
    private final HangarProjectsDAO hangarProjectsDAO;
    private final JobService jobService;

    @Autowired
    public ProjectVisibilityService(HangarDao<VisibilityDAO> visibilityDAO, HangarDao<ProjectsDAO> projectsDAO, HangarDao<HangarProjectsDAO> hangarProjectsDAO, JobService jobService) {
        super(ProjectVisibilityChangeTable::new);
        this.projectsDAO = projectsDAO.get();
        this.visibilityDAO = visibilityDAO.get();
        this.hangarProjectsDAO = hangarProjectsDAO.get();
        this.jobService = jobService;
    }

    @Override
    void logVisibilityChange(ProjectTable model, Visibility oldVisibility) {
        userActionLogService.project(LogAction.PROJECT_VISIBILITY_CHANGED.create(ProjectContext.of(model.getId()), model.getVisibility().getName(), oldVisibility.getName()));
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
    protected void postUpdate(@Nullable ProjectTable model) {
        if (model != null) {
            jobService.save(new UpdateDiscourseProjectTopicJob(model.getId()));
        }
        hangarProjectsDAO.refreshHomeProjects();
    }

    @Override
    public Entry<String, ProjectVisibilityChangeTable> getLastVisibilityChange(long principalId) {
        return visibilityDAO.getLatestProjectVisibilityChange(principalId);
    }
}
