package io.papermc.hangar.components.index;

import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.version.Version;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IndexService {

    private final IndexDAO indexDAO;
    private final MeiliService meiliService;

    public IndexService(final IndexDAO indexDAO, final MeiliService meiliService) {
        this.indexDAO = indexDAO;
        this.meiliService = meiliService;
    }

    public void fullUpdateProjects() {
        // TODO index switch instead of full update?
        List<Project> projects = this.indexDAO.getAllProjects();
        this.meiliService.sendProjects(projects);
    }

    public void updateProject(long id) {
        // TODO
    }

    public void removeProject(long id) {
        // TODO
    }

    public void fullUpdateVersions() {
        // TODO index switch instead of full update?
        List<Version> versions = this.indexDAO.getAllVersions();
        this.meiliService.sendVersions(versions);
    }

    public void updateVersion(long id) {
        // TODO
    }
}
