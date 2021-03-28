package io.papermc.hangar.serviceold.project;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.ProjectVersionDao;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.modelold.viewhelpers.VersionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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

    public void prepareDeleteVersion(VersionData versionData) {
        if (versionData.getP().getVisibility() == Visibility.SOFTDELETE) return;
        List<ProjectVersionsTable> projectVersions = projectVersionDao.get().getProjectVersions(versionData.getP().getProject().getId());
        if (projectVersions.stream().filter(p -> p.getVisibility() == Visibility.PUBLIC).count() <= 1 && versionData.getV().getVisibility() == Visibility.PUBLIC) {
            throw new HangarException("error.version.onlyOnePublic");
        }

        ProjectVersionsTable recommended = versionData.getP().getRecommendedVersion();
        if (recommended != null && versionData.getV().getId() == recommended.getId()) { // pick new recommended
            Optional<ProjectVersionsTable> tableOptional = projectVersions.stream().filter(v -> v.getId() != versionData.getV().getId() && v.getVisibility() != Visibility.SOFTDELETE).findFirst();
            tableOptional.ifPresent(projectVersionsTable -> {
                versionData.getP().getProject().setRecommendedVersionId(projectVersionsTable.getId());
                projectDao.get().update(versionData.getP().getProject());
            });
        }
    }

}
