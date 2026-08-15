package io.papermc.hangar.components.index;

import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.version.Version;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    private final IndexDAO indexDAO;
    private final MeiliService meiliService;

    public IndexService(final IndexDAO indexDAO, final MeiliService meiliService) {
        this.indexDAO = indexDAO;
        this.meiliService = meiliService;
    }

    public void fullUpdateProjects() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Starting full update projects");
        List<Project> projects = this.indexDAO.getAllProjects("");
        logger.info("Got {} projects after {}s", projects.size(), Duration.between(now, LocalDateTime.now()).toSeconds());
        this.meiliService.setupProjectIndex("-new");
        this.meiliService.waitForTask(this.meiliService.sendDocuments(MeiliService.PROJECT_INDEX + "-new", projects));
        this.meiliService.swapIndexes(MeiliService.PROJECT_INDEX, MeiliService.PROJECT_INDEX + "-new");
        logger.info("Finished full update projects after {}s", Duration.between(now, LocalDateTime.now()).toSeconds());
    }

    public void updateProject(long id) {
        List<Project> projects = this.indexDAO.getAllProjects("WHERE p.id = " + id + " LIMIT 1");
        this.meiliService.sendDocuments(MeiliService.PROJECT_INDEX, projects);
    }

    // a user's name is denormalized into every project and version document they are involved in
    public void updateUserProjects(long userId) {
        String memberProjects = "(SELECT pma.id FROM project_members_all pma WHERE pma.user_id = " + userId + ")";
        List<Project> projects = this.indexDAO.getAllProjects("WHERE p.id IN " + memberProjects);
        if (!projects.isEmpty()) {
            this.meiliService.sendDocuments(MeiliService.PROJECT_INDEX, projects);
        }
        List<Version> versions = this.indexDAO.getAllVersions("WHERE pv.author_id = " + userId + " OR pv.project_id IN " + memberProjects);
        if (!versions.isEmpty()) {
            this.meiliService.sendDocuments(MeiliService.VERSION_INDEX, versions);
        }
    }

    public void removeProject(long id) {
        this.meiliService.removeDocument(MeiliService.PROJECT_INDEX, id);
    }

    public void fullUpdateVersions() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Starting full update versions");
        List<Version> versions = this.indexDAO.getAllVersions("");
        logger.info("Got {} versions after {}s", versions.size(), Duration.between(now, LocalDateTime.now()).toSeconds());
        this.meiliService.setupVersionIndex("-new");
        this.meiliService.waitForTask(this.meiliService.sendDocuments(MeiliService.VERSION_INDEX + "-new", versions));
        this.meiliService.swapIndexes(MeiliService.VERSION_INDEX, MeiliService.VERSION_INDEX + "-new");
        logger.info("Finished full update versions after {}s", Duration.between(now, LocalDateTime.now()).toSeconds());
    }

    public void updateVersion(long id) {
        List<Version> versions = this.indexDAO.getAllVersions("WHERE pv.id = " + id + " LIMIT 1");
        this.meiliService.sendDocuments(MeiliService.VERSION_INDEX, versions);
    }

    public void removeVersion(long id) {
        this.meiliService.removeDocument(MeiliService.VERSION_INDEX, id);
    }
}
