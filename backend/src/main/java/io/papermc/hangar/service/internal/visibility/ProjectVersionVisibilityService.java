package io.papermc.hangar.service.internal.visibility;

import io.papermc.hangar.db.dao.internal.table.VisibilityDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.visibility.ProjectVersionVisibilityChangeTable;
import io.papermc.hangar.model.internal.job.UpdateDiscourseProjectTopicJob;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.service.internal.JobService;
import io.papermc.hangar.service.internal.UserActionLogService;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectVersionVisibilityService extends VisibilityService<VersionContext, ProjectVersionTable, ProjectVersionVisibilityChangeTable> {

    private final ProjectVersionsDAO projectVersionsDAO;
    private final VisibilityDAO visibilityDAO;
    private final JobService jobService;

    @Autowired
    public ProjectVersionVisibilityService(final VisibilityDAO visibilityDAO, final ProjectVersionsDAO projectVersionDAO, final JobService jobService, final UserActionLogService userActionLogService) {
        super(ProjectVersionVisibilityChangeTable::new, LogAction.VERSION_VISIBILITY_CHANGED);
        this.visibilityDAO = visibilityDAO;
        this.projectVersionsDAO = projectVersionDAO;
        this.jobService = jobService;
    }

    @Override
    void updateLastVisChange(final long currentUserId, final long modelId) {
        this.visibilityDAO.updateLatestVersionChange(currentUserId, modelId);
    }

    @Override
    ProjectVersionTable getModel(final long id) {
        return this.projectVersionsDAO.getProjectVersionTable(id);
    }

    @Override
    ProjectVersionTable updateModel(final ProjectVersionTable model) {
        return this.projectVersionsDAO.update(model);
    }

    @Override
    void insertNewVisibilityEntry(final ProjectVersionVisibilityChangeTable visibilityTable) {
        this.visibilityDAO.insert(visibilityTable);
    }

    @Override
    protected void postUpdate(final @Nullable ProjectVersionTable model) {
        if (model != null) {
            this.jobService.save(new UpdateDiscourseProjectTopicJob(model.getProjectId()));
        }
    }

    @Override
    public Map.Entry<String, ProjectVersionVisibilityChangeTable> getLastVisibilityChange(final long principalId) {
        return this.visibilityDAO.getLatestVersionVisibilityChange(principalId);
    }
}

