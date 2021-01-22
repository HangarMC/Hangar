package io.papermc.hangar.service.apiold;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.daoold.HangarDao;
import io.papermc.hangar.db.daoold.api.ProjectsApiDao;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.modelold.generated.Project;
import io.papermc.hangar.modelold.generated.ProjectMember;
import io.papermc.hangar.modelold.generated.ProjectSortingStrategy;
import io.papermc.hangar.modelold.generated.ProjectStatsDay;
import io.papermc.hangar.modelold.generated.Tag;
import io.papermc.hangar.service.pluginupload.ProjectFiles;
import io.papermc.hangar.util.ApiUtil;
import io.papermc.hangar.util.Routes;
import io.papermc.hangar.util.TemplateHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectApiService {

    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectsApiDao> projectApiDao;
    private final ProjectFiles projectFiles;
    private final TemplateHelper templateHelper;

    public ProjectApiService(HangarConfig hangarConfig, HangarDao<ProjectsApiDao> projectApiDao, ProjectFiles projectFiles, TemplateHelper templateHelper) {
        this.hangarConfig = hangarConfig;
        this.projectApiDao = projectApiDao;
        this.projectFiles = projectFiles;
        this.templateHelper = templateHelper;
    }

    public Project getProject(long id, boolean seeHidden, Long requesterId) {
        Project project = projectApiDao.get().listProjects(id, seeHidden, requesterId, null, null, null, null, null, 1, 0).stream().findFirst().orElse(null);
        if (project != null) {
            setProjectIconUrl(project);
        }
        return project;
    }

    private void setProjectIconUrl(Project project) {
        Path iconPath = projectFiles.getIconPath(project.getNamespace().getOwner(), project.getNamespace().getSlug());
        if (iconPath != null) {
            project.setIconUrl(hangarConfig.getBaseUrl() + Routes.getRouteUrlOf("projects.showIcon", project.getNamespace().getOwner(), project.getNamespace().getSlug()));
        } else {
            project.setIconUrl(templateHelper.avatarUrl(project.getNamespace().getOwner()));
        }
    }

    public List<ProjectMember> getProjectMembers(String author, String slug, long limit, long offset) {
        return projectApiDao.get().projectMembers(author, slug, limit, offset);
    }

    public Map<String, ProjectStatsDay> getProjectStats(String author, String slug, LocalDate fromDate, LocalDate toDate) {
        return projectApiDao.get().projectStats(author, slug, fromDate, toDate);
    }
}
