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
        List<Project> projects = this.indexDAO.getAllProjects("");
        this.meiliService.setupProjectIndex("-new");
        this.meiliService.waitForTask(this.meiliService.sendDocuments(MeiliService.PROJECT_INDEX + "-new", projects));
        this.meiliService.swapIndexes(MeiliService.PROJECT_INDEX, MeiliService.PROJECT_INDEX + "-new");
    }

    public void updateProject(long id) {
        List<Project> projects = this.indexDAO.getAllProjects("p.id = " + id + " LIMIT 1");
        this.meiliService.sendDocuments(MeiliService.PROJECT_INDEX, projects);
    }

    public void removeProject(long id) {
        this.meiliService.removeDocument(MeiliService.PROJECT_INDEX, id);
    }

    public void fullUpdateVersions() {
        List<Version> versions = this.indexDAO.getAllVersions("");
        this.meiliService.setupVersionIndex("-new");
        this.meiliService.waitForTask(this.meiliService.sendDocuments(MeiliService.VERSION_INDEX + "-new", versions));
        this.meiliService.swapIndexes(MeiliService.VERSION_INDEX, MeiliService.VERSION_INDEX + "-new");
    }

    public void updateVersion(long id) {
        List<Version> versions = this.indexDAO.getAllVersions("v.id = " + id + " LIMIT 1");
        this.meiliService.sendDocuments(MeiliService.VERSION_INDEX, versions);
    }

    public void removeVersion(long id) {
        this.meiliService.removeDocument(MeiliService.VERSION_INDEX, id);
    }
}
