package io.papermc.hangar.serviceold.project;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.ProjectVersionDao;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.model.common.projects.Visibility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("oldProjectFactory")
@Deprecated(forRemoval = true)
public class ProjectFactory {

    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<ProjectVersionDao> projectVersionDao;
    private final ProjectService projectService;

    @Autowired
    public ProjectFactory(HangarDao<ProjectDao> projectDao, HangarDao<ProjectVersionDao> projectVersionDao, ProjectService projectService) {
        this.projectDao = projectDao;
        this.projectVersionDao = projectVersionDao;
        this.projectService = projectService;
    }

    public void softDeleteProject(ProjectsTable projectsTable, String comment) {
        if (projectsTable.getVisibility() == Visibility.NEW) {
            hardDeleteProject(projectsTable);
            return;
        }
        projectService.changeVisibility(projectsTable, Visibility.SOFTDELETE, comment);
    }

    public void hardDeleteProject(ProjectsTable projectsTable) {
        projectDao.get().delete(projectsTable);
    }

}
