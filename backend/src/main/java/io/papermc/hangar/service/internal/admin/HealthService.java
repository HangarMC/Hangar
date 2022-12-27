package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HealthDAO;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthService extends HangarComponent {

    private final HealthDAO healthDAO;
    private final ProjectFiles projectFiles;
    private final FileService fileService;

    @Autowired
    public HealthService(final HealthDAO healthDAO, final ProjectFiles projectFiles, final FileService fileService) {
        this.healthDAO = healthDAO;
        this.projectFiles = projectFiles;
        this.fileService = fileService;
    }

    public List<UnhealthyProject> getProjectsWithoutTopic() {
        return this.healthDAO.getProjectsWithoutTopic();
    }

    public List<UnhealthyProject> getStaleProjects() {
        return this.healthDAO.getStaleProjects("'" + this.config.projects.staleAge().toSeconds() + " SECONDS'");
    }

    public List<UnhealthyProject> getNonPublicProjects() {
        return this.healthDAO.getNonPublicProjects();
    }

    public List<MissingFileCheck> getVersionsWithMissingFiles() {
        final List<MissingFileCheck> missingFileChecks = this.healthDAO.getVersionsForMissingFiles();
        return missingFileChecks.stream().filter(mfc -> {
            final String path = this.projectFiles.getVersionDir(mfc.getNamespace().getOwner(), mfc.getName(), mfc.getVersionString(), mfc.getPlatform());
            return !this.fileService.exists(this.fileService.resolve(path, mfc.getFileName()));
        }).toList();
    }
}
