package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.projects.PinnedProjectDAO;
import io.papermc.hangar.model.api.project.ProjectCompact;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PinnedProjectService extends HangarComponent {

    private final PinnedProjectDAO pinnedProjectDAO;

    @Autowired
    public PinnedProjectService(final PinnedProjectDAO pinnedProjectDAO) {
        this.pinnedProjectDAO = pinnedProjectDAO;
    }

    public void addPinnedProject(final long userId, final long projectId) {
        this.pinnedProjectDAO.insert(userId, projectId);
    }

    public void removePinnedProject(final long userId, final long projectId) {
        this.pinnedProjectDAO.delete(userId, projectId);
    }

    public List<ProjectCompact> getPinnedProjects(final long userid) {
        return this.pinnedProjectDAO.pinnedProjects(userid);
    }
}
